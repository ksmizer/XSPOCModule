package com.avadine.xspoc.collector;

import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.web.components.RecordActionTable;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * CollectorConfigurationPage
 */
public class CollectorConfigurationPage extends RecordActionTable<CollectorConfiguration> {

    transient List<ICalculatedField<CollectorConfiguration>> calcFields;

    public static ConfigCategory CONFIG_CATEGORY = new ConfigCategory("xspoc", "Collector.MenuTitle");
    public static IConfigTab MENU_ENTRY = DefaultConfigTab.builder()
            .category(CONFIG_CATEGORY)
            .name("Collector")
            .i18n("Collector.Setup.MenuTitle")
            .page(CollectorConfigurationPage.class)
            .terms("Collector")
            .build();

    public CollectorConfigurationPage(IConfigPage configPage) {
        super(configPage);
    }

    @Override
    protected RecordMeta<CollectorConfiguration> getRecordMeta() {
        return CollectorConfiguration.META;
    }

    @Override
    public Pair<String, String> getMenuLocation() {
        return MENU_ENTRY.getMenuLocation();
    }

    @Override
    protected String getTitleKey() {
        return "Collector.Setup.PageTitle";
    }

    @Override
    protected List<ICalculatedField<CollectorConfiguration>> getCalculatedFields() {
        if (calcFields == null) {
            calcFields = new ArrayList<>(1);
            calcFields.add(new ICalculatedField<CollectorConfiguration>() {
                @Override
                public String getFieldvalue(CollectorConfiguration record) {
                    return Boolean.toString(record.getBoolean(record.Enabled));
                }

                @Override
                public String getHeaderKey() {
                    return "Collector.isEnabled.Name";
                }
            });
        }
        return calcFields;
    }

    @Override
    protected String getNoRowsKey() {
        return "Collector.Setup.NoRows";
    }
}