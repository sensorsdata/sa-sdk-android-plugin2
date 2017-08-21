package com.sensorsdata.analytics.android.plugin

class SensorsAnalyticsExtension {

    List<String> includeJarFilter = new ArrayList<String>()
    List<String> excludeJarFilter = new ArrayList<String>()
    List<String> ajcArgs = new ArrayList<>();

    public SensorsAnalyticsExtension includeJarFilter(String... filters) {
        if (filters != null) {
            includeJarFilter.addAll(filters)
        }

        return this
    }

    public SensorsAnalyticsExtension excludeJarFilter(String... filters) {
        if (filters != null) {
            excludeJarFilter.addAll(filters)
        }

        return this
    }

    public SensorsAnalyticsExtension ajcArgs(String... ajcArgs) {
        if (ajcArgs != null) {
            this.ajcArgs.addAll(ajcArgs)
        }
        return this
    }
}

