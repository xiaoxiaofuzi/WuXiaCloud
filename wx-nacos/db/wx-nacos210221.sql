/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : wx-nacos

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2021-02-24 10:26:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `content` longtext COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `c_use` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `effect` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `c_schema` text COLLATE utf8_bin,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES ('1', 'application-dev.yml', 'DEFAULT_GROUP', 0x737072696E673A0A20206D61696E3A0A20202020616C6C6F772D6265616E2D646566696E6974696F6E2D6F766572726964696E673A20747275650A20206175746F636F6E6669677572653A0A202020206578636C7564653A20636F6D2E616C69626162612E64727569642E737072696E672E626F6F742E6175746F636F6E6669677572652E447275696444617461536F757263654175746F436F6E6669677572650A0A23E8AFB7E6B182E5A484E79086E79A84E8B685E697B6E697B6E997B40A726962626F6E3A0A20205265616454696D656F75743A2031303030300A2020436F6E6E65637454696D656F75743A2031303030300A0A2320666569676E20E9858DE7BDAE0A666569676E3A0A202073656E74696E656C3A0A20202020656E61626C65643A20747275650A20206F6B687474703A0A20202020656E61626C65643A20747275650A202068747470636C69656E743A0A20202020656E61626C65643A2066616C73650A2020636C69656E743A0A20202020636F6E6669673A0A20202020202064656661756C743A0A2020202020202020636F6E6E65637454696D656F75743A2031303030300A20202020202020207265616454696D656F75743A2031303030300A2020636F6D7072657373696F6E3A0A20202020726571756573743A0A202020202020656E61626C65643A20747275650A20202020726573706F6E73653A0A202020202020656E61626C65643A20747275650A0A2320E69AB4E99CB2E79B91E68EA7E7ABAFE782B90A6D616E6167656D656E743A0A2020656E64706F696E74733A0A202020207765623A0A2020202020206578706F737572653A0A2020202020202020696E636C7564653A20272A270A, 'c07e6f7321493f6d5390d0a08bffb75a', '2019-11-29 16:31:20', '2020-12-21 15:29:24', '', '0:0:0:0:0:0:0:1', '', '', '通用配置', 'null', 'null', 'yaml', 0x6E756C6C);
INSERT INTO `config_info` VALUES ('23', 'wx-gateway-dev.yml', 'DEFAULT_GROUP', 0x737072696E673A0A2020636C6F75643A0A20202020676174657761793A0A20202020202068747470636C69656E743A0A2020202020202020706F6F6C3A0A202020202020202020206D61782D69646C652D74696D653A20353030300A202020202020202020206D61782D636F6E6E656374696F6E733A2033300A2020202020206C6F616462616C616E6365723A0A20202020202020207573653430343A2066616C73650A20202020202064656661756C742D66696C746572733A0A2020202020202020202D20446564757065526573706F6E73654865616465723D4163636573732D436F6E74726F6C2D416C6C6F772D4F726967696E0A202020202020676C6F62616C636F72733A0A20202020202020206164642D746F2D73696D706C652D75726C2D68616E646C65722D6D617070696E673A20747275650A2020202020202020636F7273436F6E66696775726174696F6E733A0A20202020202020202020275B2F2A2A5D273A0A202020202020202020202020616C6C6F7765642D6F726967696E733A20222A220A202020202020202020202020616C6C6F7765642D6D6574686F64733A20222A220A202020202020202020202020616C6C6F7765642D686561646572733A20222A220A202020202020202020202020616C6C6F772D63726564656E7469616C733A20747275650A202020202020646973636F766572793A0A20202020202020206C6F6361746F723A0A2020202020202020202023E698AFE5B086E8AFB7E6B182E8B7AFE5BE84E4B88AE79A84E69C8DE58AA1E5908DE9858DE7BDAEE4B8BAE5B08FE58699EFBC88E59BA0E4B8BAE69C8DE58AA1E6B3A8E5868CE79A84E697B6E58099EFBC8CE59091E6B3A8E5868CE4B8ADE5BF83E6B3A8E5868CE697B6E5B086E69C8DE58AA1E5908DE8BDACE68890E5A4A7E58699E79A84E4BA860A202020202020202020206C6F776572436173655365727669636549643A20747275650A2020202020202020202023E8A1A8E6988E67617465776179E5BC80E590AFE69C8DE58AA1E6B3A8E5868CE5928CE58F91E78EB0E79A84E58A9FE883BDEFBC8CE5B9B6E4B894737072696E6720636C6F75642067617465776179E887AAE58AA8E6A0B9E68DAEE69C8DE58AA1E58F91E78EB0E4B8BAE6AF8FE4B880E4B8AAE69C8DE58AA1E5889BE5BBBAE4BA86E4B880E4B8AA726F75746572EFBC8CE8BF99E4B8AA726F75746572E5B086E4BBA5E69C8DE58AA1E5908DE5BC80E5A4B4E79A84E8AFB7E6B182E8B7AFE5BE84E8BDACE58F91E588B0E5AFB9E5BA94E79A84E69C8DE58AA10A20202020202020202020656E61626C65643A20747275650A202020202020726F757465733A0A20202020202020202320E6B58BE8AF950A20202020202020202D2069643A2077782D63657368690A202020202020202020207572693A206C623A2F2F77782D63657368690A20202020202020202020707265646963617465733A0A2020202020202020202020202D2052657175657374426F64793D504F53542C5055540A2020202020202020202020202D20506174683D2F63657368692F2A2A2C2F6365736869312F2A2A0A2020202020202020202066696C746572733A0A2020202020202020202020202D2043655368693D32342C67682CE794B70A2020202020202020202020202D2053747269705072656669783D31, 'e0f40694931c8a3af90080d99d6896ca', '2021-02-03 09:33:08', '2021-02-24 00:59:29', null, '10.200.1.173', '', '', '', '', '', 'yaml', '');
INSERT INTO `config_info` VALUES ('43', 'sentinel-wx-gateway', 'DEFAULT_GROUP', 0x5B0D0A202020207B0D0A2020202020202020227265736F75726365223A202277782D6365736869222C0D0A202020202020202022636F756E74223A20352C0D0A2020202020202020226772616465223A20302C0D0A2020202020202020226C696D6974417070223A202264656661756C74222C0D0A2020202020202020227374726174656779223A20302C0D0A202020202020202022636F6E74726F6C4265686176696F72223A20300D0A202020207D0D0A5D, 'd79ecfd43a58179a090c6b79448788e4', '2021-02-04 06:45:56', '2021-02-22 06:41:14', null, '10.200.1.173', '', '', '111', '', '', 'json', '');
INSERT INTO `config_info` VALUES ('44', 'wx-ceshi-dev.yml', 'DEFAULT_GROUP', 0x63657368693A203132340A2020, 'd00fc6228769dbe6da289af81a79d6cf', '2021-02-21 08:50:50', '2021-02-21 08:51:24', null, '10.200.1.173', '', '', '', '', '', 'yaml', '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_beta';

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_tag';

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation` (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`),
  UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_tag_relation';

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群、各Group容量信息表';

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info` (
  `id` bigint(64) unsigned NOT NULL,
  `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `group_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext COLLATE utf8_bin NOT NULL,
  `md5` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text COLLATE utf8_bin,
  `src_ip` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `op_type` char(10) COLLATE utf8_bin DEFAULT NULL,
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`nid`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_gmt_modified` (`gmt_modified`),
  KEY `idx_did` (`data_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='多租户改造';

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info` VALUES ('0', '1', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233, '4d2f2a7d8ae055875802e3e05ce50d1b', '2021-01-20 15:56:16', '2021-01-20 07:56:17', null, '10.200.1.83', 'I', '');
INSERT INTO `his_config_info` VALUES ('0', '2', 'application-dev.yml', 'DEFAULT_GROUP', '', 0x2320E69AB4E99CB2E79B91E68EA7E7ABAFE782B90D0A6D616E6167656D656E743A0D0A2020656E64706F696E74733A0D0A202020207765623A0D0A2020202020206578706F737572653A0D0A2020202020202020696E636C7564653A20272A270D0A20202020202020206578636C7564653A206865617064756D702C64756D702C74687265616464756D702C636F6E66696770726F70732C656E760D0A, '7bb474f29a534a058c4d96faf1d38946', '2021-01-20 15:58:11', '2021-01-20 07:58:12', null, '10.200.1.83', 'I', '');
INSERT INTO `his_config_info` VALUES ('1', '3', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233, '4d2f2a7d8ae055875802e3e05ce50d1b', '2021-01-20 16:50:21', '2021-01-20 08:50:21', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '4', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20343536, '2343a8c702dc1cfcd41726d0a5b29cae', '2021-01-20 16:52:41', '2021-01-20 08:52:41', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '5', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20343536323133, 'ef22d52f242117d6555fc04b073d46e7', '2021-01-20 16:54:21', '2021-01-20 08:54:22', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '6', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20343536323133333234, 'a4a436e2bb2405f20996c582ad473cad', '2021-01-20 16:59:06', '2021-01-20 08:59:06', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '7', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A203435, '24ed27c24ff7562a7f1c1dd98b275812', '2021-01-20 17:02:52', '2021-01-20 09:02:52', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('0', '8', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313132323333, '3040824b42c91e3d800ea693a69b3225', '2021-01-20 17:03:58', '2021-01-20 09:03:58', null, '10.200.1.83', 'I', '');
INSERT INTO `his_config_info` VALUES ('1', '9', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A2034353231333132, 'b260549366a000c9e70bdaebdc8093b4', '2021-01-20 17:04:17', '2021-01-20 09:04:17', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('8', '10', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313132323333, '3040824b42c91e3d800ea693a69b3225', '2021-01-20 17:04:36', '2021-01-20 09:04:36', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('8', '11', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A203132333434353536, 'd2ef831615dce678e84c047a5cd6a6de', '2021-01-20 17:06:17', '2021-01-20 09:06:18', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('8', '12', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233323133313233, '8c7a70154e61a8927779b2b3aa99ea1c', '2021-01-20 17:11:19', '2021-01-20 09:11:20', null, '10.200.1.83', 'D', '');
INSERT INTO `his_config_info` VALUES ('1', '13', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x61613A2034353231333132, 'ac988fa75ea3c351e20d7a8bd3596f9a', '2021-01-20 17:11:31', '2021-01-20 09:11:31', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '14', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A2034353231333132, 'b260549366a000c9e70bdaebdc8093b4', '2021-01-20 17:20:28', '2021-01-20 09:20:29', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '15', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A2034353231333132, 'b260549366a000c9e70bdaebdc8093b4', '2021-01-20 17:25:05', '2021-01-20 09:25:06', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '16', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233, '4d2f2a7d8ae055875802e3e05ce50d1b', '2021-01-20 17:31:33', '2021-01-20 09:31:34', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '17', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A203131313131, 'f7947505b40314d8268d43f45b3e496f', '2021-01-20 17:33:33', '2021-01-20 09:33:34', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '18', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A2031323334, '4613517b1319434c8c84b0910d938a09', '2021-01-20 17:35:25', '2021-01-20 09:35:25', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '19', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A203132333132333132, '7ca3a1d07f708aa8de56e7daceb4d680', '2021-01-20 17:37:00', '2021-01-20 09:37:00', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '20', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A2031313131, '9f31c8c214d763511d195bfa8b08f2f1', '2021-01-20 17:43:35', '2021-01-20 09:43:35', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('1', '21', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313131313131313131, '65c4e72560213c586b13cd16d75807e7', '2021-01-20 17:45:45', '2021-01-20 09:45:45', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('2', '22', 'application-dev.yml', 'DEFAULT_GROUP', '', 0x2320E69AB4E99CB2E79B91E68EA7E7ABAFE782B90D0A6D616E6167656D656E743A0D0A2020656E64706F696E74733A0D0A202020207765623A0D0A2020202020206578706F737572653A0D0A2020202020202020696E636C7564653A20272A270D0A20202020202020206578636C7564653A206865617064756D702C64756D702C74687265616464756D702C636F6E66696770726F70732C656E760D0A, '7bb474f29a534a058c4d96faf1d38946', '2021-01-20 17:48:33', '2021-01-20 09:48:33', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('0', '23', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20797979, '790cfde4c84f33ca399262388397b4e5', '2021-01-20 17:49:30', '2021-01-20 09:49:31', null, '10.200.1.83', 'I', '');
INSERT INTO `his_config_info` VALUES ('1', '24', 'wx-gateway-test.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233, '4d2f2a7d8ae055875802e3e05ce50d1b', '2021-01-20 17:49:50', '2021-01-20 09:49:51', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '25', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20797979, '790cfde4c84f33ca399262388397b4e5', '2021-01-20 17:51:21', '2021-01-20 09:51:22', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '26', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233, '4d2f2a7d8ae055875802e3e05ce50d1b', '2021-01-20 17:51:55', '2021-01-20 09:51:55', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '27', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20666666, 'd9f3fd6380f973367fa4e4fbe22a4749', '2021-01-20 17:55:55', '2021-01-20 09:55:55', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '28', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233313233, 'a1f016455b640e12c7b4bb5d53b6d73f', '2021-01-20 17:58:02', '2021-01-20 09:58:03', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '29', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20313233313233, 'a1f016455b640e12c7b4bb5d53b6d73f', '2021-01-20 17:58:41', '2021-01-20 09:58:41', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '30', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A203132, 'c08a11dd604d98b44ec701283927fc2f', '2021-01-20 18:01:11', '2021-01-20 10:01:12', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '31', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A2031333432, '002329c6855feffbe99b436d176d1629', '2021-01-20 18:08:06', '2021-01-20 10:08:07', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '32', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A2031313131, '9f31c8c214d763511d195bfa8b08f2f1', '2021-01-20 18:08:20', '2021-01-20 10:08:21', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '33', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A20333333, 'e9fc1f629da10d26d81dce7c343c177a', '2021-01-20 18:10:58', '2021-01-20 10:10:58', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '34', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A2031313131313131313131, '10c46144a4891d380c7956954934e072', '2021-01-20 18:15:10', '2021-01-20 10:15:10', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '35', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A206466, '825451371bc4703021054f0b0f680ca0', '2021-01-20 18:40:27', '2021-01-20 10:40:28', null, '10.200.1.83', 'U', '');
INSERT INTO `his_config_info` VALUES ('22', '36', 'wx-gateway.yml', 'DEFAULT_GROUP', '', 0x63657368693A207676767676, 'ef976fda425b66684035cea0416dcb2a', '2021-01-20 18:42:22', '2021-01-20 10:42:22', null, '10.200.1.83', 'U', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `role` varchar(50) NOT NULL,
  `resource` varchar(255) NOT NULL,
  `action` varchar(8) NOT NULL,
  UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `username` varchar(50) NOT NULL,
  `role` varchar(50) NOT NULL,
  UNIQUE KEY `idx_user_role` (`username`,`role`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='租户容量信息表';

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='tenant_info';

-- ----------------------------
-- Records of tenant_info
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(500) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', '1');
