package com.wx.wxcommoncache.utils;

import com.alibaba.fastjson.JSONObject;
import com.wx.wxcommoncache.service.CacheManager;
import com.wx.wxcommoncore.dto.cache.DbDictBean;
import com.wx.wxcommoncore.dto.cache.DictBean;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author gh
 * @ClassName DictCacheUtils
 * @Description 这里的缓存结构会一直存在内存中，不会进行删除。
 *
 *                提供功能：1. 查询某一个字典类型（大类）的字典数据（顶层结构）（主要用于前台请求）
 *                          2. 根据 字典类型 和 code 查询某一个字典数据（主要用于前台请求）
 *                          3. 根据 字典类型 和 code 查询 字典名称（主要用于展示前台 code转name，代码中调用 ）
 *                          4. 根据缓存字典类型和字典名称，查询 code（主要用于导入功能， name 转 code）
 *                 功能 2,3 由于是递归查询，是否可以考虑可以将查询出的结果进行 一个短时间的缓存
 *
 * <p>
 * 数据类型：{@link com.wx.wxcommoncore.dto.cache.DictBean}
 * @Date 2020/9/27 0027 17:22
 * @Version 1.0
 **/
public class DictCacheUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(DictCacheUtils.class);

    /**
     * -- 获取版本的lua脚本，当版本号超过 100 重置为1，由于 hmest lua 做不到传对象，生成版本号 和 生成缓存 不做同步
     * -- KEYS[1]:表示版本号
     *
     * 返回新的版本号
     */
    private static final String SCRIPT_LUA_OF_GET_VERSION =
            "if redis.call('EXISTS',KEYS[1]) == 1 then \n" +
                    " local version = redis.call('INCRBY',KEYS[1],1) " +
                    " if(version > 100) then " +
                    " redis.call('SET',KEYS[1],1) " +
                    " return 1" +
                    " else " +
                    " return version " +
                    "end" +
                    " else\n" +
                    "  redis.call('SET',KEYS[1],1)\n" +
                    "  return 1\n" +
                    " end";

    /**
     * 字典缓存刷新锁key 共有字符串，最终形成key为 ： 传入的缓存key: cacheKey+LOCK_KEY
     */
    private static final String LOCK_KEY = ":DICT:CACHE:LOCK";
    /**
     * 字典版本缓存key 共有字符串，最终形成key为 ： 传入的缓存key: cacheKey+VERSION_KEY
     */
    private static final String VERSION_KEY = ":DICT:VERSION";
    /**
     * 字典结构缓存前缀， 共有字符串，最终形成key为 ： 传入的缓存key: cacheKey+DICT_KEY_PREFIX+VERSION(当前版本号)
     */
    private static final String DICT_KEY_PREFIX = ":DICT:DICT:";
    /**
     * 字典名称和字典值对应的缓存前缀， 共有字符串，最终形成key为 ： 传入的缓存key: cacheKey+NAME_CODE_KEY_PREFIX+VERSION(当前版本号)
     */
    private static final String NAME_CODE_KEY_PREFIX = ":DICT:NAME:CODE:";

    /**
     * 是否使用转换功能: 消耗时间(调用方法)来将层级结构 转换为 平面结构
     * 主要用于 将 层级的缓存结构 --> 平面的 name-code 的缓存结构
     *
     * 如果为false: 会在刷新缓存的同时 将生成的  name-code 平面结构保存到redis中,省去项目刚启动时需要将层级结构转换为平面结构的时间，增加了redis内存
     * 如果为true: 第一次加载时，会调用方法将 层级机构 转换为 name-code 结构，不保存在redis，缓存刷新也不会保存在redis.
     *
     * 默认 开启，影响时间很小，在内存中操作，没有IO操作。
     */
    private static final boolean USE_THE_TRANSFORMATION_FUNCTION = true;

    /**
     * 延时3秒删除本地过期版本
     */
    private static final int DELAY_3_SECONDS = 3;

    /**
     * 刷新版本监听器第一次执行的延迟时间
     */
    private static final int REFRESH_INITIAL_DELAY = 60;

    /**
     * 刷新版本监听器执行的延迟时间
     */
    private static final int REFRESH_PERIOD = 10;
    /**
     * 缓存管理，这个里面记录着 所有的缓存，以后如果要监听这些缓存，可以操作这个结构
     */
    private static Map<String,AbstractCache> DICT_CACHE = new ConcurrentHashMap<>(16);
    /**
     * redis 缓存--存储
     */
    private static CacheManager cache;
    /**
     * redission 锁--加锁
     */
    private static CacheManager lockManager;

    static {
        cache = CacheUtil.getCache();
        lockManager = CacheUtil.getLockManager();
    }

    /**
     * 功能描述: 生成缓存,只需要传入 唯一缓存key,以及 加载数据库数据的方法即可( 实现 DictDataInterface)
     *
     * @author gh
     * @Date 2020/9/28 0028 
     * @param cacheKey 生成/获取 缓存的唯一key
     * @param dictDataInterface 获取数据的实现类
     * @return java.lang.String
     **/       
    public static String putCache(String cacheKey,DictDataInterface dictDataInterface){

        AbstractCache cache = new AbstractCache(cacheKey, dictDataInterface) {
            @Override
            protected String getLockKey() {
                return cacheKey + LOCK_KEY;
            }

            @Override
            protected String getVersionKey() {
                return cacheKey + VERSION_KEY;
            }

            @Override
            protected String getDictKey() {
                return cacheKey + DICT_KEY_PREFIX;
            }

            @Override
            protected String getNameCodeKey() {
                return cacheKey + NAME_CODE_KEY_PREFIX;
            }
        };

        // 1. 加载redis缓存到本地服务
        cache.loadTheRedisDictionaryIntoTheLocalCache();
        // 2. 注册监听器，监听redis版本情况，单连接池
        cache.registerRedisVersionListener();
        // 3. 注册延时删除过期版本 单连接池
        cache.registerDelayedDelExpiredVersionThreadPool();

        DICT_CACHE.put(cacheKey,cache);

        return "V"+cache.currentCacheVersion;
    }

    public static AbstractCache removeCache(String cacheKey){
        return DICT_CACHE.remove(cacheKey);
    }

    /**
     * 功能1:  获取某一个字典类型的所有字典数据(顶层结构)
     * @param dictCacheKey 缓存key
     */
    public static Map<String, DictBean> getByDictId(String dictCacheKey) {
        return getByDictId(dictCacheKey,dictCacheKey);
    }

    /**
     * 功能1:  获取某一个字典类型的所有字典数据(层级结构)
     * @param dictCacheKey 缓存key
     * @param dictId 字典类型
     */
    public static Map<String, DictBean> getByDictId(String dictCacheKey, String dictId) {
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.getByDictId(dictId);
    }

    /**
     * 功能2:  根据 字典类型 和 code 查询某一个字典数据（层级结构）
     * @param dictCacheKey 缓存key
     */
    public static DictBean getByDictIdAndCode(String dictCacheKey,String code) {
        return getByDictIdAndCode(dictCacheKey,dictCacheKey,code);
    }

    /**
     * 功能2: 根据 字典类型 和 code 查询某一个字典数据（层级结构）
     * @param dictCacheKey 缓存key
     * @param dictId 字典类型
     */
    public static DictBean getByDictIdAndCode(String dictCacheKey, String dictId,String code) {
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.getDictBean(dictId,code);
    }


    /**
     * 功能3: 根据缓存字典类型和字典值，查询字典名称   dictId + code  -->  name
     * @param dictCacheKey 缓存key
     * @param code 字典值
     * @return
     */
    public static String getName(String dictCacheKey, String code) {
        return getName(dictCacheKey,dictCacheKey,code);
    }

    /**
     * 功能3: 根据缓存字典类型和字典值，查询字典名称
     * @param dictCacheKey 缓存key
     * @param dict 字典类型
     * @param code 字典值
     * @return
     */
    public static String getName(String dictCacheKey, String dict, String code) {
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.getName(dict,code);
    }


    /**
     * 功能4: 根据缓存key 和 字典名称 ，查询字典值
     * @param dictCacheKey 缓存key
     * @param name 字典名称
     * @return
     */
    public static String[] getCodeByName(String dictCacheKey,String[] name) {
        return getCodeByName(dictCacheKey,dictCacheKey,name);
    }

    /**
     * 功能4: 根据缓存字典类型和字典名称，查询字典值
     * @param dictCacheKey 缓存key
     * @param name 字典名称
     * @return
     */
    public static String[] getCodeByName(String dictCacheKey, String dictId, String[] name) {
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.getCodeByName(dictId,name);
    }

    /**
     * 功能描述: 刷新缓存
     **/
    public static String refreshCache(String dictCacheKey){
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.genRedisCacheByDb(true);
    }

    public static boolean shutdownThreadPool(String dictCacheKey){
        AbstractCache caChe = DICT_CACHE.get(dictCacheKey);
        return caChe.shutdownThreadPool();
    }

    /**
     *  查询数据接口
     */
    public interface DictDataInterface {


    /**
     * 查询下一层的字典数据
     * @param codeList code 集合
     * @param dictIdList dictId集合
     * @return
     */
       List<DbDictBean> getOtherLayersData(List<String> codeList, List<String> dictIdList);

        /**
         * 查询顶层的字典数据
         * @return
         */
       List<DbDictBean> getTopData();
    }


    /**
     * 缓存类
     */
    private static abstract class AbstractCache{

        /**
         * 缓存key,必须唯一
         */
        private String cacheKey;

        private DictDataInterface dictDataInterface;


        /**
         * 单线程池---监听redis中 字典的版本号，和服务中的缓存版本比较，如果不一致，需要取最新的缓存进行替换。
         *
         * 10秒监听一次
         */
        private ScheduledExecutorService redisVersionListener;

        /**
         * 延时删除过期版本缓存
         */
        private ScheduledExecutorService delayedDelExpiredVersionThreadPool;

        AbstractCache(String cacheKey, DictDataInterface dictDataInterface){
            this.cacheKey = cacheKey;
            this.dictDataInterface = dictDataInterface;
        }

        /**
         * 字典值缓存版本，当每一次刷新缓存的时候增加一条数据,并删除上一个版本
         * key: 版本号
         * value：(Map){
         * key: 字典类型
         * value:(Map<code，DictBean>){
         * code:字典值，
         * name:字典名称,
         * children:(Map<code，DictBean>){...}
         * }
         * }
         */
        private Map<Integer, Map<String, Map<String, DictBean>>> versionCache = new ConcurrentHashMap<>(16);


        /**
         * 版本号:map
         *  字典类型：map
         *    字典name_层级_pCode: 字典code
         */
        private Map<Integer, Map<String,Map<String,String>>> versionNameCodeCache = new HashMap<>();

        /**
         * 当前本地服务记录的版本号
         */
        private volatile int currentCacheVersion;

        /**
         * 刷新缓存所需要的 分布式锁key
         * @return
         */
        protected abstract String getLockKey();
        /**
         * 缓存版本key
         * @return
         */
        protected abstract String getVersionKey();

        /**
         * 缓存结构key
         * @return
         */
        protected abstract String getDictKey();

        /*
         * 缓存字典名称和 code 对应的 key
         * @return
         */
        protected abstract String getNameCodeKey();


        /**
         * 功能描述: 加载redis字典缓存到本地服务
         *          首先判断是否存在版本号，不存在说明是第一次加载，获取redis缓存锁，刷新redis缓存。
         *                                  存在则刷新到本地
         *
         * @author gh
         * @Date 2020/9/24 0024
         * @return void
         **/
        private void loadTheRedisDictionaryIntoTheLocalCache() {

            String versionKey = getVersionKey();

            Boolean exists = cache.exists(versionKey);
            if (!exists) {
                //初始化：获取redis 字典缓存的锁
                RLock lock = lockManager.getLock(getLockKey());
                try {
                    //默认30秒过期
                    lock.lock();
                    //再次判断是否初始化成功
                    exists = cache.exists(versionKey);
                    if(!exists){
                        genRedisCacheByDb(false);
                        return;
                    }
                }catch (Exception e){
                    LOGGER.error("{}缓存获取redis分布式锁失败",cacheKey,e);
                }finally {
                    lock.unlock();
                }
            }
            //redis中存在字典缓存，需要配置本地缓存
            //当前版本
            int currentVersion = (int) cache.get(versionKey);
            //redis层级结构
            Map<Object, Object> dictBeanMap = cache.hget(getDictKey() + currentVersion);
            if(!USE_THE_TRANSFORMATION_FUNCTION){
                Map<Object, Object> nameCodeMap = cache.hget(getNameCodeKey() + currentVersion);
                loadNewVersionToLocal(currentVersion,dictBeanMap,nameCodeMap);
            }else {
                loadNewVersionToLocal(currentVersion,dictBeanMap);
            }
            LOGGER.info("{}缓存加载到本地成功，版本V{}",cacheKey,currentVersion);
        }



        /**
         * 功能描述: 注册版本监听器，用来监听redis中字典的最新版本，看是否与本地map缓存一致。
         * 不一致情况：由于查询字典都是查询的本地map缓存，在分布式环境中（假设A B C三台服务），当A服务器刷新了缓存，只有A本地缓存更新，B C依然记录着上个版本的缓存
         *             所以需要有一个线程一直监听新版本，如果发现不一致，需要更新当前本地map缓存，并删除其他过期版本。
         * @author gh
         * @Date 2020/9/24 0024
         * @return void
         **/
        private void registerRedisVersionListener() {
            //单线程池
            redisVersionListener = Executors.newSingleThreadScheduledExecutor(new DictCacheVersionThreadFactory(cacheKey));
            //任务
            Runnable runnable = ()->{
                try{
                    //redis 中字典版本
                    int dictVersion = (int) cache.get(getVersionKey());
                    if(dictVersion != currentCacheVersion){
                        LOGGER.info("redis字典版本和本地字典不一致：[V{}/V{}],刷新缓存中。。。",dictVersion,currentCacheVersion);
                        //层级结构
                        Map<Object, Object> cacheMap = cache.hget(getDictKey() + dictVersion);
                        //如果发生并发(生成版本和生成缓存不是原子操作)，可能为空，由于 生成版本号和 生成缓存不是同步执行
                        if(cacheMap != null){
                            if(!USE_THE_TRANSFORMATION_FUNCTION){
                                Map<Object, Object> nameCodeMap = cache.hget(getNameCodeKey() + dictVersion);
                                loadNewVersionToLocal(dictVersion,cacheMap, nameCodeMap);
                            }else {
                                loadNewVersionToLocal(dictVersion,cacheMap);
                            }
                            delExpiredVersion();
                            LOGGER.info("刷新缓存成功");
                        }else {
                            LOGGER.info("刷新缓存失败(存在版本号，但没有对应缓存数据)，等待下一次刷新");
                        }
                    }
                }catch (Exception e){
                    LOGGER.error(e.getMessage(),e);
                }
            };
            //1分钟后开始 每10秒执行一次任务
            redisVersionListener.scheduleAtFixedRate(runnable,REFRESH_INITIAL_DELAY,REFRESH_PERIOD,TimeUnit.SECONDS);
        }




        /**
         * 功能描述: 注册延时删除过期版本 单连接池
         *
         * @author gh
         * @Date 2020/9/24 0024
         * @return void
         **/
        private void registerDelayedDelExpiredVersionThreadPool() {
            DelayedDeleteExpiredDictionaryVersionThreadFactory threadFactory = new DelayedDeleteExpiredDictionaryVersionThreadFactory(cacheKey);
            delayedDelExpiredVersionThreadPool =Executors.newSingleThreadScheduledExecutor(threadFactory);
        }


        /**
         * 获取缓存：项目启动，将redis中的字典缓存加载到本地，并开启版本监听器，刷新本地缓存。用户查询时，通过本地查询
         */
        private Map<String, DictBean> getByDictId(String dictId) {
            Map<String, Map<String, DictBean>> stringMapMap = versionCache.get(currentCacheVersion);
            return stringMapMap != null ? stringMapMap.get(dictId) : null;
        }



        /**
         * 功能描述: 通过 code 查询 name, 通过递归，不是很好，可以考虑将  每一层结构做缓存，目前先这样实现。
         *
         * @param dict 类型
         * @param code code
         * @return java.lang.String
         * @author gh
         * @Date 2020/9/11 0021
         **/
        public String getName(String dict, String code) {
            Map<String, DictBean> dictMap = getDictMapByDictId(dict);
            //递归查询
            return getNameByCode(code, dictMap);
        }

        /**
         * 功能描述: 递归查询
         *
         * @param code    类型
         * @param dictMap 字典值
         * @return java.lang.String
         * @author gh
         * @Date 2020/9/11 0021
         **/
        private String getNameByCode(String code, Map<String, ?> dictMap) {

            DictBean dictBean = (DictBean) dictMap.get(code);
            //是否是当前层数的字典值
            if (dictBean != null) {
                return dictBean.getName();
            } else {
                //循环当前层级，取下一层进行递归查询。
                for (Map.Entry<String, ?> entry : dictMap.entrySet()) {

                    DictBean value = (DictBean) entry.getValue();

                    JSONObject childrenNew = value.getChildren();

                    if (childrenNew.size() > 0) {
                        String nameByChildren = getNameByCode(code, childrenNew);
                        if (nameByChildren != null) {
                            return nameByChildren;
                        }
                    }

                }
            }
            return null;
        }



        /**
         * 功能描述: 通过 code 查询 name, 通过递归，不是很好，可以考虑将  每一层结构做缓存，目前先这样实现。
         *
         * @param dict 类型
         * @param code code
         * @return java.lang.String
         * @author gh
         * @Date 2020/9/11 0021
         **/
        DictBean getDictBean(String dict, String code) {

            Map<String, DictBean> dictMap = getDictMapByDictId(dict);
            //递归查询
            return getDictBeanByCode(code, dictMap);
        }

        /**
         * 功能描述: 递归查询
         *
         * @param code    类型
         * @param dictMap 字典值
         * @return java.lang.String
         * @author gh
         * @Date 2020/9/11 0021
         **/
        private DictBean getDictBeanByCode(String code, Map<String, ?> dictMap) {

            DictBean dictBean = (DictBean) dictMap.get(code);
            //是否是当前层数的字典值
            if (dictBean != null) {
                return dictBean;
            } else {
                //循环当前层级，取下一层进行递归查询。
                for (Map.Entry<String, ?> entry : dictMap.entrySet()) {

                    DictBean value = (DictBean) entry.getValue();

                    JSONObject childrenNew = value.getChildren();

                    if (childrenNew.size() > 0) {
                        DictBean sonDictBean = getDictBeanByCode(code, childrenNew);
                        if (sonDictBean != null) {
                            return sonDictBean;
                        }
                    }

                }
            }
            return null;
        }



        /**
         * 功能描述: 通过字典类型查询出 此字典类型的平面结构map
         *
         *           循环name（顺序，从前到后表示 从第1层级往后），按层级查找平面结构
         *
         *           当有一层没有找到之后就直接返回已经查询到的数据，后面返回“”
         *
         * @author gh
         * @Date 2020/9/30 0030
         * @param dict 字典类型
         * @param name 层级字典名称，数组，必须从上层到下层
         * @return java.lang.String
         **/
        String[] getCodeByName(String dict, String[] name){

            int length;
            if(name == null || (length = name.length) == 0){
                return null;
            }

            Map<String, Map<String, String>> stringMapMap = versionNameCodeCache.get(currentCacheVersion);

            Map<String, String> nameCodeMap = stringMapMap.get(dict);

            if(nameCodeMap == null){
//                LOGGER.error("缓存中不存在字典类型：{},请查看是否填写正确，如果填写正确，请点击刷新缓存后查询。",dict);
//                return null;
                throw new RuntimeException(String.format("缓存中不存在字典类型：{%s},请查看是否填写正确，如果填写正确，请点击刷新缓存后查询。",dict));
            }
            String[] codes = new String[length];
            String post;
            String code = "";
            for(int level = 0; level < length ;level++){
                if(level == 0){
                    post = name[level]+"_"+level;
                }else {
                    post = name[level]+"_"+level + "_"+code;
                }
                code = nameCodeMap.get(post);

                //如果code 找不到，就停止查找
                if(code == null){
                    return codes;
                }else {
                    codes[level] = code;
                }
            }
            return codes;
        }

        private Map<String,DictBean> getDictMapByDictId(String dict) {
            Map<String, Map<String, DictBean>> currentMapCache = versionCache.get(currentCacheVersion);
            //获取当前字典类型的字典结构
            Map<String, DictBean> dictMap = currentMapCache.get(dict);
            if(dictMap == null){
//                LOGGER.error("缓存中不存在字典类型：{},请查看是否填写正确，如果填写正确，请点击刷新缓存后查询。",dict);
//                return null;
                throw new RuntimeException(String.format("缓存中不存在字典类型：{%s},请查看是否填写正确，如果填写正确，请点击刷新缓存后查询。",dict));
            }
            return dictMap;
        }

        private void loadNewVersionToLocal(int newVersion, Map<Object, Object> dictBeanMap, Map<Object, Object> nameCodeMap) {

            Map<String, Map<String, DictBean>> tmpMap = new HashMap<>(16);
            for(Map.Entry<Object,Object> entry : dictBeanMap.entrySet()){
                String key = (String) entry.getKey();
                Map<String, DictBean> value = (Map<String, DictBean>) entry.getValue();
                tmpMap.put(key,value);
            }

            Map<String,Map<String,String>> tmpNameCodeMap = new HashMap<>(16);
            for(Map.Entry<Object,Object> entry : nameCodeMap.entrySet()){
                String key = (String) entry.getKey();
                Map<String, String> value = (Map<String, String>) entry.getValue();
                tmpNameCodeMap.put(key,value);
            }
            //刷新到本地
            versionCache.put(newVersion,tmpMap);
            versionNameCodeCache.put(newVersion,tmpNameCodeMap);
            currentCacheVersion = newVersion;
        }

        private void loadNewVersionToLocal(int newVersion, Map<Object, Object> dictBeanMap) {

            Map<String, Map<String, DictBean>> tmpMap = new HashMap<>(16);
            for(Map.Entry<Object,Object> entry : dictBeanMap.entrySet()){
                String key = (String) entry.getKey();
                Map<String, DictBean> value = (Map<String, DictBean>) entry.getValue();
                tmpMap.put(key,value);
            }
            //刷新到本地
            versionCache.put(newVersion,tmpMap);
            versionNameCodeCache.put(newVersion,dictCacheToNameCodeCache(tmpMap));
            currentCacheVersion = newVersion;
        }


        /**
         * 功能描述: 刷新缓存：获得刷新锁（避免这次还没刷新完成，下次刷新又开始了），生成新的层级结构和字典结构（redis和本地），然后刷新版本号结构，删除上次版本号缓存。
         *      获取db字典结构： 由于不知道字典层数，通过 循环来构造 字典缓存接口。 采用分层思想（空间换时间）来进行构造
         *                       TODO 还未优化,如果数据有问题，会报错，应该提示，不应该报错
         *                       preMap: 每查询一层，需要将这一层的 结构记录，提供给下一层找到。
         *
         * redis 中数据结构:
         * <p>
         * 版本号结构：使用 string(key,value)
         * key: FOOD:DICT:VERSION
         * value: $V
         * </p>
         *
         * <p>
         * 字典结构：使用 hash(key,field,value)
         * key: FOOD:DICT:DICT:$V
         * field: $DICTID
         * value：当前类型层级字典结构
         * </p>
         * @author gh
         * @Date 2020/9/7 0007
         *
         * @param isExpiredVersion 是否删除过期版本
         *
         * @return boolean
         **/
        public String genRedisCacheByDb(boolean isExpiredVersion) {
            //获取 刷新缓存key, 字典缓存同步进行
            RLock lock = lockManager.getLock(getLockKey());
            try {
                boolean isLock = lock.tryLock(3, 30, TimeUnit.SECONDS);
                if(!isLock){
                    return String.format("%s缓存有人在同步中，请稍后再试",cacheKey);
                }

                //字典名称和code 缓存
                Map<String,Map<String,String>> nameCodeCache = null;
                //缓存结构
                Map<String, Map<String, DictBean>> mapCache = new HashMap<>(16);
                //每一层的结构，每找一层覆盖一次。主要用途：下一层找到自己的父类
                Map<String, Map<String, DictBean>> preMap = new HashMap<>(16);
                //下一层查询条件
                List<String> codeList = new ArrayList<>();
                List<String> dictIdList = new ArrayList<>();
                try {

                    //每一层字典数据,这是顶层
                    List<DbDictBean> tDictCodeCaseList = dictDataInterface.getTopData();
                    //最上层逻辑
                    if (!CollectionUtils.isEmpty(tDictCodeCaseList)) {
                        nameCodeCache = genTopDictBean(mapCache, tDictCodeCaseList, preMap, codeList, dictIdList);
                        //最大层级目前设置为10级，如果超过说明数据有问题。
                        int deptMax = 10;
                        int depth = 0;
                        //循环查询，直到没有下一层
                        while (true) {
                            //查询非顶层的数据
                            tDictCodeCaseList = dictDataInterface.getOtherLayersData(codeList, dictIdList);
                            if(++depth > deptMax){
                                return String.format("数据异常，超过最大层级%d,查询数据为：%s",deptMax,codeList);
                            }
                            //清空条件，释放内存
                            codeList.clear();
                            dictIdList.clear();
                            //如果还有值 说明有子集，需要填充孩子
                            if (!CollectionUtils.isEmpty(tDictCodeCaseList)) {
                                genNextDictBean(tDictCodeCaseList, preMap, codeList, dictIdList,depth,nameCodeCache);
                            } else {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("生成缓存数据发生异常：{}", e.getMessage(), e);
                    return String.format("生成缓存数据发生异常：%s",e.getMessage());
                }
                String versionKey = getVersionKey();
                String dictKey = getDictKey();
                //redis 当前缓存
                Object redisVersion = cache.get(versionKey);
                Long version = cache.get(SCRIPT_LUA_OF_GET_VERSION, Collections.singletonList(versionKey), Long.class);
                cache.hmset( dictKey+ version,mapCache);

                if(!USE_THE_TRANSFORMATION_FUNCTION){
                    cache.hmset( getNameCodeKey()+ version,nameCodeCache);
                }
                int newVersion = Math.toIntExact(version);
                versionCache.put(newVersion,mapCache);
                versionNameCodeCache.put(newVersion,nameCodeCache);
                currentCacheVersion = newVersion;
                LOGGER.info("{}缓存刷新成功，版本V{}",cacheKey,currentCacheVersion);
                if(isExpiredVersion){
                    //删除上一个版本的redis缓存，以及本地缓存
                    cache.del(dictKey+redisVersion);
                    if(!USE_THE_TRANSFORMATION_FUNCTION){
                        cache.del(getNameCodeKey()+ redisVersion);
                    }
                    delExpiredVersion();
                }
            }catch (Exception e){
                LOGGER.error("刷新缓存失败",e);
                return String.format("刷新缓存失败：%s",e.getMessage());
            }finally {
                lock.unlock();
            }
            return "刷新缓存成功";
        }

        void delExpiredVersion(){
            delayedDelExpiredVersionThreadPool.schedule(()->{
                LOGGER.info("当前版本V{}，删除其他版本中...",currentCacheVersion);
                Set<Integer> removeVersions = versionCache.keySet();
                StringBuilder msg = new StringBuilder();
                for(Integer removeVersion : removeVersions){
                    if(removeVersion != currentCacheVersion){
                        msg.append("{V").append(removeVersion).append("}");
                        versionCache.remove(removeVersion);
                        versionNameCodeCache.remove(removeVersion);
                    }
                }
                LOGGER.info("删除其他版本{}成功",msg);
            },DELAY_3_SECONDS,TimeUnit.SECONDS);
        }

        /**
         * 功能描述: 对非顶层数据进行封装
         *
         * @param tDictCodeCaseList 查询出一层的 字典数据
         * @param preMap            上一层的结构数据
         * @param codeList          查询下一层的 pcode 条件
         * @param dictIdList        查询下一层的 dictId 条件
         * @param depth              深度，层级
         * @param nameCodeCache     name 和 code 的对应关系
         * @author gh
         * @Date 2020/9/11 0011
         **/
        private void genNextDictBean(List<DbDictBean> tDictCodeCaseList, Map<String, Map<String, DictBean>> preMap, List<String> codeList, List<String> dictIdList, int depth, Map<String, Map<String, String>> nameCodeCache) {
            //临时数据，用来封装这一层的字典数据 tDictCodeCaseList，将来替换 preMap
            Map<String, Map<String, DictBean>> tempPreMap = new HashMap<>(16);

            for (DbDictBean tDictCodeCase : tDictCodeCaseList) {

                String code = tDictCodeCase.getCode();
                String dictId = StringUtils.isNotBlank(tDictCodeCase.getDictId()) ? tDictCodeCase.getDictId() : cacheKey ;
                String name = tDictCodeCase.getName();
                String pCode = tDictCodeCase.getParentCode();


                //添加对应关系
                Map<String, String> nameCodeMap = nameCodeCache.get(dictId);
                nameCodeMap.put(name+"_"+depth+"_"+pCode,code);

                //从上一层中获取值
                Map<String, DictBean> fatherDictBeans = preMap.get(dictId);
                DictBean fatherDictBean = fatherDictBeans.get(pCode);

                //填入到孩子集合中
                JSONObject children = fatherDictBean.getChildren();
                DictBean build = DictBean.builder().code(code).name(name).build();
                children.put(code, build);

                //封装此层的结构数据，提供下一层使用
                Map<String, DictBean> sonDictBeans = tempPreMap.computeIfAbsent(dictId, k -> new HashMap<>(16));

                sonDictBeans.put(code, build);

                //下一次循环查询条件
                codeList.add(code);
                dictIdList.add(dictId);
            }

            //替换当前层字典数据
            preMap.clear();
            preMap.putAll(tempPreMap);
        }

        /**
         * 功能描述: 生成最上层的字典数据结构
         *
         * @param mapCache          最终缓存数据
         * @param tDictCodeCaseList 查询出最上层的 字典数据
         * @param preMap            对 tDictCodeCaseList 进行数据封装过的 结构
         * @param codeList          查询下一层的 pcode 条件
         * @param dictIdList        查询下一层的 dictId 条件
         * @author gh
         * @Date 2020/9/11 0011
         **/
        private Map<String, Map<String, String>> genTopDictBean(Map<String, Map<String, DictBean>> mapCache, List<DbDictBean> tDictCodeCaseList, Map<String, Map<String, DictBean>> preMap, List<String> codeList, List<String> dictIdList) {
            Map<String,Map<String,String>> nameCodeCache = new HashMap<>(16);
            Map<String, String> tempNameCodeCache = null;

            //上一个字典类型，由于查询是按字典类型排序，所以有规律可循
            String preDictId = null;
            //临时数据
            Map<String, DictBean> tempMap = null;

            for (DbDictBean dict : tDictCodeCaseList) {

                String code = dict.getCode();
                String dictId = StringUtils.isNotBlank(dict.getDictId()) ? dict.getDictId() : cacheKey ;
                String name = dict.getName();

                if (!dictId.equals(preDictId)) {
                    //新的字典类型
                    tempMap = new HashMap<>(16);
                    tempNameCodeCache = new HashMap<>(16);
                    mapCache.put(dictId, tempMap);
                    preMap.put(dictId, tempMap);
                    nameCodeCache.put(dictId,tempNameCodeCache);
                    preDictId = dictId;
                } else {
                    tempMap = mapCache.get(dictId);
                    tempNameCodeCache = nameCodeCache.get(dictId);
                }
                tempNameCodeCache.put(name+"_0",code);
                DictBean build = DictBean.builder().code(code).name(name).build();
                tempMap.put(code, build);

                //下一次循环查询条件
                codeList.add(code);
                dictIdList.add(dictId);
            }
            return nameCodeCache;
        }

        boolean shutdownThreadPool(){
            //关闭线程池
            if(redisVersionListener != null && !redisVersionListener.isShutdown()){
                redisVersionListener.shutdown();
            }
            LOGGER.info("{}监听器销毁成功",cacheKey);

            if(delayedDelExpiredVersionThreadPool != null && delayedDelExpiredVersionThreadPool.isShutdown()){
                delayedDelExpiredVersionThreadPool.shutdown();
            }
            LOGGER.info("{}定时删除器销毁成功",cacheKey);
            return true;
        }

        /**
         * 功能描述: 将层级结构的字典缓存进行重组形成一个平面化的 name-code 映射 关系缓存
         *
         * @author gh
         * @Date 2020/10/9 0009 
         * @param stringMapMap 层级字典缓存
         * @return java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>
         **/       
        private Map<String,Map<String,String>> dictCacheToNameCodeCache(Map<String, Map<String, DictBean>> stringMapMap){
            //字典类型的 name--code 映射缓存
            Map<String,Map<String,String>> dictNameCodeMap = new HashMap<>(16);

            for(Map.Entry<String,Map<String, DictBean>> entry : stringMapMap.entrySet()){

                //一个字典类型 一个字典 name--code 映射
                Map<String,String>  nameCodeMap = new HashMap<>(32);
                String key = entry.getKey();

                dictNameCodeMap.put(key,nameCodeMap);

                Map<String, DictBean> dictCache = entry.getValue();

                int level = 0;
                //将层级结构平面化
                cacheLevelPlanarization("",level,dictCache,nameCodeMap);
            }

            return dictNameCodeMap;
        }


        /**
         * 功能描述: 将层级结构平面化
         *
         * @author gh
         * @Date 2020/10/9 0009 
         * @param pCode 父code
         * @param level 层级
         * @param value 字典值
         * @param nameCodeMap 一个字典类型的name-code映射
         * @return void
         **/       
        private void cacheLevelPlanarization(String pCode,int level, Map<String, ?> value, Map<String, String> nameCodeMap) {

            //循环当前层级，取下一层进行递归查询。
            for (Map.Entry<String, ?> entry : value.entrySet()) {

                DictBean dictBean = (DictBean) entry.getValue();

                String name = dictBean.getName();

                String code = dictBean.getCode();

                String keyName = name + "_" + level;

                if(level > 0){
                    keyName+="_"+pCode;
                }

                nameCodeMap.put(keyName,code);

                JSONObject childrenNew = dictBean.getChildren();

                if (childrenNew.size() > 0) {
                    //每一层需要新的层级变量
                    int newLevel = level+1;
                    cacheLevelPlanarization(code, newLevel,childrenNew,nameCodeMap);
                }
            }
        }


        /**
         * 测试使用, 测试通过，不在使用
         * @param dictNameCodeMap 层级 -> 平面的缓存
         */
//         void comparison(Map<String,Map<String,String>> dictNameCodeMap){
//
//            System.out.println("============对比开始============");
//
//            Map<String, Map<String, String>> stringMapMap1 = versionNameCodeCache.get(currentCacheVersion);
//
//            for(Map.Entry<String,Map<String, String>> entry : stringMapMap1.entrySet()){
//
//                String key = entry.getKey();
//                System.out.println("检查字典类型："+key);
//                boolean containsKey = dictNameCodeMap.containsKey(key);
//
//                if(!containsKey){
//                    System.out.println(String.format("不正确,没有发现字典类型[%s]",key));
//                    continue;
//                }
//
//                Map<String, String> value = entry.getValue();
//                for(Map.Entry<String,String> entry1 : value.entrySet()){
//
//                    String key1 = entry1.getKey();
//
//                    String value1 = entry1.getValue();
//                    Map<String, String> stringStringMap = dictNameCodeMap.get(key);
//                    boolean containsKey1 = stringStringMap.containsKey(key1);
//                    if(!containsKey1){
//                        System.out.println(String.format("不正确,没有发现字典名称[%s]",key1));
//                        continue;
//                    }
//                    String s = stringStringMap.get(key1);
//                    if(!value1.equals(s)){
//                        System.out.println(String.format("不正确,字典名称[%s]的字典值不正确：[%s] -------  [%s]",key1,value1,s));
//                    }
//                }
//            }
//
//            System.out.println("============对比结束============");
//
//        }
    }


    /**
     * 监听缓存版本
     */
    static class DictCacheVersionThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String name;

        DictCacheVersionThreadFactory(String cacheKey) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            name = "[" +cacheKey + "]缓存版本监听器";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    name,
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }


    /**
     * 延时删除版本
     */
    static class DelayedDeleteExpiredDictionaryVersionThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String name;

        DelayedDeleteExpiredDictionaryVersionThreadFactory(String cacheKey) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            name = "[" +cacheKey + "]删除过期版本";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    name,
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            t.setUncaughtExceptionHandler((thread,throwable)->{
                LOGGER.error(throwable.getMessage());
            });

            return t;
        }
    }




}
