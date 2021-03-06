package com.yitulin.dubbocall.infrastructure.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.yitulin.dubbocall.domain.request.entity.HttpRequestCallLogEntity;
import com.yitulin.dubbocall.domain.request.entity.HttpRequestTemplateEntity;
import com.yitulin.dubbocall.domain.request.entity.SettingAggregate;
import com.yitulin.dubbocall.domain.request.entity.VariableEntity;
import com.yitulin.dubbocall.infrastructure.common.Constants;
import com.yitulin.dubbocall.infrastructure.utils.JsonFileUtil;
import com.yitulin.dubbocall.infrastructure.utils.PropertiesComponentUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;

/**
 * @author : β‘οΈ
 * description :
 * date : Created in 2021/9/7 5:19 δΈε
 * modified : π§π¨π₯
 */
@Data
public class SettingService {

    private static final Log LOG = LogFactory.get();

    private String configFilePath="";
    private File configFile;
    private String logFilePath="";
    private File logFile;

    private SettingAggregate settingAggregate;

    private Map<String, HttpRequestCallLogEntity> requestCallLogEntityMap=Maps.newHashMap();

    public static SettingService getInstance(){
        Supplier<SettingService> supplier = Suppliers.memoize(new Supplier<SettingService>() {
            @Override
            public SettingService get() {
                SettingService settingService = new SettingService();
                String configFilePath = PropertiesComponentUtil.read(Constants.CONFIG_FILE_PATH_KEY);
                if (Strings.isNullOrEmpty(configFilePath)) {
                    settingService.settingAggregate =new SettingAggregate(true);
                    return settingService;
                }
                if (!FileUtil.exist(configFilePath)){
                    settingService.settingAggregate =new SettingAggregate(true);
                    return settingService;
                }
                settingService.resetConfigFile(configFilePath);
                return settingService;
            }
        });
        return supplier.get();
    }

    public boolean configFileExists(){
        return Objects.nonNull(configFile) && configFile.exists();
    }

    public boolean logFileExists(){
        return Objects.nonNull(logFile) && logFile.exists();
    }

    public boolean containsTemplate(String templateName){
        List<String> collect = this.settingAggregate.getHttpRequestTemplateEntityList().stream().map(HttpRequestTemplateEntity::getName).collect(Collectors.toList());
        return collect.contains(templateName);
    }

    public void overrideConfigFile(){
        LOG.info("ζ Ήζ?εε­δΈ­ηιη½?ζ°ζ?θ¦ηιη½?ζδ»Ά,this.settingAggregate:[{}]", settingAggregate);
        if (!this.configFileExists()){
            LOG.info("ιη½?ζδ»ΆδΈε­ε¨οΌζ ζ³θ¦η", settingAggregate);
            return;
        }
        JsonFileUtil.overrideWrite(configFile, JSONUtil.parse(settingAggregate));
    }

    public void overrideLogFile(){
        LOG.info("ζ Ήζ?εε­δΈ­ηθ?°ε½ζ°ζ?θ¦ηθ?°ε½ζδ»Ά,this.requestCallLogEntityMap:[{}]", requestCallLogEntityMap);
        if (!this.logFileExists()){
            LOG.info("θ?°ε½ζδ»ΆδΈε­ε¨οΌζ ζ³θ¦η", requestCallLogEntityMap);
            return;
        }
        JsonFileUtil.overrideWrite(logFile, JSONUtil.parse(requestCallLogEntityMap));
    }

    public void resetConfigFile(String configFilePath){
        LOG.info("ζ Ήζ?ζδ»Άθ·―εΎιη½?ιη½?ζ°ζ?,params:[{}]",configFilePath);
        this.configFilePath=configFilePath;
        PropertiesComponentUtil.write(Constants.CONFIG_FILE_PATH_KEY,this.configFilePath);
        this.configFile = new File(configFilePath);
        this.logFilePath=this.configFile.getParentFile().getPath()+"/dubbo-call-log.json";
        this.logFile = new File(this.logFilePath);
        this.resetFromConfigFile();
    }

    public void resetConfigFile(File configFile){
        LOG.info("ζ Ήζ?ζδ»Άιη½?ιη½?ζ°ζ?,params:[{}]",configFile.getPath());
        this.configFilePath=configFile.getPath();
        PropertiesComponentUtil.write(Constants.CONFIG_FILE_PATH_KEY,this.configFilePath);
        this.configFile = configFile;
        this.logFilePath=this.configFile.getParentFile().getPath()+"/dubbo-call-log.json";
        this.logFile = new File(this.logFilePath);
        this.resetFromConfigFile();
    }

    public void initLogFile(){
        if (this.logFile.exists()){
            return;
        }
        try {
            this.logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.overrideLogFile();
    }

    public void resetFromConfigFile(){
        JSON json = JsonFileUtil.readFile(this.configFile);
        LOG.info("ιη½?ιη½?ζ°ζ?,json:[{}]",json);
        JSONObject jsonObject= (JSONObject) json;
        String defaultTemplateName = ((JSONObject) json).getStr("defaultTemplateName");
        JSONArray variableEntityList = ((JSONObject) json).getJSONArray("variableEntityList");
        List<VariableEntity> variableEntities = variableEntityList.toList(VariableEntity.class);
        JSONArray httpRequestTemplateEntityList = ((JSONObject) json).getJSONArray("httpRequestTemplateEntityList");
        List<HttpRequestTemplateEntity> httpRequestTemplateEntities = httpRequestTemplateEntityList.toList(HttpRequestTemplateEntity.class);
        this.settingAggregate = SettingAggregate.builder()
                .defaultTemplateName(defaultTemplateName)
                .variableEntityList(variableEntities)
                .httpRequestTemplateEntityList(httpRequestTemplateEntities)
                .build();
        this.initLogFile();
        JSON logJson = JsonFileUtil.readFile(this.logFile);
        LOG.info("ιη½?θ?°ε½ζ°ζ?,logJson:[{}]",logJson);
        JSONObject logJsonObject= (JSONObject) logJson;
        logJsonObject.keySet().stream().forEach(key->{
            HttpRequestCallLogEntity httpRequestCallLogEntity = logJsonObject.get(key, HttpRequestCallLogEntity.class);
            requestCallLogEntityMap.put(key,httpRequestCallLogEntity);
        });
    }

}
