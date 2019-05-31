package com.avadine.lego.collector;

import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.EncodedStringField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.EnumField;
import com.inductiveautomation.ignition.gateway.datasource.records.DatasourceRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IntField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.LongField;
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

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
    )
    .setNounKey("Collector.Noun")
    .setNounPluralKey("Collector.Noun.Plural");
    
    public static final IdentityField Id = new IdentityField(META, "Id");       
    
    /* Use existing connection */
    public static final LongField DatabaseId = new LongField(META, "DatabaseId");
    public static final ReferenceField<DatasourceRecord> Existing = new ReferenceField<>(META, DatasourceRecord.META, "Existing", DatabaseId);

    /* Create separate connection */
    public static final StringField Server = new StringField(META, "Server", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final IntField Port = new IntField(META, "Port", SFieldFlags.SMANDATORY);
    public static final StringField Database = new StringField(META, "Database", SFieldFlags.SMANDATORY);
    public static final StringField Username = new StringField(META, "Username", SFieldFlags.SMANDATORY);
    public static final EncodedStringField Password = new EncodedStringField(META, "Password", SFieldFlags.SMANDATORY);
    
    /* Collector Ids */
    public static final StringField CollectorIds = new StringField(META, "CollectorIds").addValidator(new SValidatorI() {
        @Override
        public void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation {
            if (!instance.isNull(field)) {
                String val = instance.getString(field);

                if (val.startsWith(",")) {
                    throw new SException.Validation("Field " + field + " cannot start with a comma");
                }

                if (!StringUtils.isNumericSpace(val.replace(",", ""))) {
                    throw new SException.Validation("Field " + field + " value must only contain numbers, spaces, and/or commas");
                }
            }
        }
    });
    public static final BooleanField Enabled = new BooleanField(META, "Enabled").setDefault(true);

    static final Category ExistingConnectionCategory = new Category("Collector.ExistingConnectionCategory.Name", 1).include(Existing);
    static final Category NewConnectionCategory = new Category("Collector.NewConnectionCategory.Name", 2, true).include(Server, Port, Database, Username, Password);
    static final Category Settings = new Category("Collector.SettingsCategory.Name", 3).include(CollectorIds, Enabled);
    

    static {
        Existing.getFormMeta().setFieldNameKey("Collector.Existing.Name");
        Existing.getFormMeta().setFieldDescriptionKey("Collector.Existing.Desc");
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
        CollectorIds.getFormMeta().setFieldNameKey("Collector.CollectorIds.Name");
        CollectorIds.getFormMeta().setFieldDescriptionKey("Collector.CollectorIds.Desc");
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

    public Boolean hasExistingConnection() {
        return !isNull(Existing);
    }
    
    public String getDatasource() {
        return getString(Existing);
    }

    public String getCollectorIdString() {
        return getString(CollectorIds);
    }

    public List<Integer> getCollectorIds() {
        List<Integer> ids = new ArrayList<Integer>();
        String idString = getCollectorIdString();
        for (String id : idString.split(",")) {
            ids.add(Integer.parseInt(id));
        }
        return ids;
    }

    public String getConnectionString() {
        return "jdbc:sqlserver://" + getServer() + "\\MSSQLSERVER:" + getPort() + ";databaseName=" + getDatabase();
    }

    public Boolean getEnabled() {
        return getBoolean(Enabled);
    }
}