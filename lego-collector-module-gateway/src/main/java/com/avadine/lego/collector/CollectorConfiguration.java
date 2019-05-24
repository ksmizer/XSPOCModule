package com.avadine.lego.collector;

import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.EncodedStringField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
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
            "CollectorConfiguration"
    ).setNounKey("Collector.Noun")
            .setNounPluralKey("Collector.Noun.Plural");
    
            
    public static final IdentityField Id = new IdentityField(META, "Id");
    public static final StringField Name = new StringField(META, "Name", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField APIToken = new StringField(META, "APIToken", SFieldFlags.SMANDATORY).addValidator(new SValidatorI() {
        @Override
        public void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation {
            if (!instance.isNull(field)) {
                String val = instance.getString(field);

                if (val.length() != 16) {
                    throw new SException.Validation("Field " + field + " value must be 16 characters");
                }

                if (!StringUtils.isAlphanumeric(val)) {
                    throw new SException.Validation("Field " + field + " value must be alphanumeric");
                }
            } else {
                throw new SException.Validation("Field " + field + " cannot be NULL");
            }
        }
    });
    public static final EncodedStringField Secret = new EncodedStringField(META, "Secret", SFieldFlags.SMANDATORY);
    public static final BooleanField Enabled = new BooleanField(META, "Enabled").setDefault(true);

    static {
        Name.getFormMeta().setFieldNameKey("NodeRED.Name.Name");
        Name.getFormMeta().setFieldDescriptionKey("NodeRED.Name.Desc");
        APIToken.getFormMeta().setFieldNameKey("NodeRED.APIToken.Name");
        APIToken.getFormMeta().setFieldDescriptionKey("NodeRED.APIToken.Desc");
        Secret.getFormMeta().setFieldNameKey("NodeRED.Secret.Name");
        Secret.getFormMeta().setFieldDescriptionKey("NodeRED.Secret.Desc");
        Secret.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
        Enabled.getFormMeta().setFieldNameKey("NodeRED.Enabled.Name");
        Enabled.getFormMeta().setFieldDescriptionKey("NodeRED.Enabled.Desc");
    }
    
    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
    
}