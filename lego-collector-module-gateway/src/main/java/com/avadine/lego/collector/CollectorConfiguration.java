package com.avadine.lego.collector;

import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.EncodedStringField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IntField;
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource;
import org.apache.commons.lang.StringUtils;

import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.validation.SValidatorI;
import simpleorm.utils.SException;

/**
 * CollectorConfiguration
 */
public class CollectorConfiguration extends PersistentRecord {
    public static final RecordMeta<CollectorConfiguration> META = new RecordMeta<>(
            CollectorConfiguration.class,
            "Collector"
    ).setNounKey("Collector.Noun")
            .setNounPluralKey("Collector.Noun.Plural");
    
    public static final IdentityField Id = new IdentityField(META, "Id");        
    public static final StringField Server = new StringField(META, "Server", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final IntField Port = new IntField(META, "Port", SFieldFlags.SMANDATORY);
    public static final StringField Database = new StringField(META, "Database", SFieldFlags.SMANDATORY);
    public static final StringField Username = new StringField(META, "Username", SFieldFlags.SMANDATORY);
    public static final EncodedStringField Password = new EncodedStringField(META, "Password", SFieldFlags.SMANDATORY);
    public static final BooleanField Enabled = new BooleanField(META, "Enabled").setDefault(true);

    static {
        Server.getFormMeta().setFieldNameKey("Collector.Server.Name");
        Server.getFormMeta().setFieldDescriptionKey("Collector.Server.Desc");
        Port.getFormMeta().setFieldNameKey("Collector.Port.Name");
        Port.getFormMeta().setFieldDescriptionKey("Collector.Port.Desc");
        Database.getFormMeta().setFieldNameKey("Collector.Database.Name");
        Database.getFormMeta().setFieldDescriptionKey("Collector.Database.Desc");
        Username.getFormMeta().setFieldNameKey("Collector.Username.Name");
        Username.getFormMeta().setFieldDescriptionKey("Collector.Username.Desc");
        Password.getFormMeta().setFieldNameKey("Collector.Password.Name");
        Password.getFormMeta().setFieldDescriptionKey("Collector.Password.Desc");
        Password.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
        Enabled.getFormMeta().setFieldNameKey("Collector.isEnabled.Name");
        Enabled.getFormMeta().setFieldDescriptionKey("Collector.isEnabled.Desc");
    }
    
    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
    
    public Long getId() {
        return getLong(Id);
    }

    public String getServer() {
        return getString(Server);
    }

    public String getDatabase() {
        return getString(Database);
    }

    public String getUsername() {
        return getString(Username);
    }

    public String getPassword() {
        return getString(Password);
    }

    public String getPort() {
        return getString(Port);
    }

    public String getConnectionString() {
        return "jdbc:sqlserver://" + getServer() + "\\MSSQLSERVER:" + getPort() + ";databaseName=" + getDatabase();
    }

    public Boolean getEnabled() {
        return getBoolean(Enabled);
    }
}