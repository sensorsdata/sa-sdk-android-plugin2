/**Created by renqingyou on 2018/12/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

package com.sensorsdata.analytics.android.plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class SensorsAnalyticsJSRAdapter extends JSRInlinerAdapter {
    protected SensorsAnalyticsJSRAdapter(int api, MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(api, mv, access, name, desc, signature, exceptions);
    }
}
