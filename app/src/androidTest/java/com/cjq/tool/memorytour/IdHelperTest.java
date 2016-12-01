package com.cjq.tool.memorytour;

import android.test.AndroidTestCase;

import com.cjq.tool.memorytour.util.IdHelper;

import junit.framework.Assert;

/**
 * Created by KAT on 2016/11/9.
 */
public class IdHelperTest extends AndroidTestCase {

    public void test_id_build() {
        String id = IdHelper.compressedUuid();
        Assert.assertNotNull(id);
    }
}
