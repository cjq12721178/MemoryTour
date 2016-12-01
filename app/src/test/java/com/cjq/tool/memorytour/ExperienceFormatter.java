package com.cjq.tool.memorytour;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by KAT on 2016/11/15.
 */
public class ExperienceFormatter {

    private String formatExperience(String experience) {
        StringBuilder builder = new StringBuilder(experience);
        for (int start = 0, size = builder.length(), spaceIndex; start < size;++start) {
            spaceIndex = start;
            //查询每段开头有几个空格
            while (spaceIndex < size && builder.charAt(spaceIndex) == '　') {
                ++spaceIndex;
            }
            //不足两个空格的补足
            while (spaceIndex - start < 2) {
                builder.insert(spaceIndex, '　');
                ++spaceIndex;
                ++size;
            }
            //多于两个空格的删除
            while (spaceIndex - start > 2) {
                builder.deleteCharAt(--spaceIndex);
                --size;
            }
            start = builder.indexOf("\n", spaceIndex);
            if (start == -1)
                break;
        }
        return builder.toString();
    }

    @Test
    public void no_space() {
        String origin = "12345\nabcde\n呵呵里个呵呵";
        String expect = "　　12345\n　　abcde\n　　呵呵里个呵呵";
        Assert.assertEquals(expect, formatExperience(origin));
    }

    @Test
    public void one_space() {
        String origin = "　12345\n　abcde\n　呵呵里个呵呵";
        String expect = "　　12345\n　　abcde\n　　呵呵里个呵呵";
        Assert.assertEquals(expect, formatExperience(origin));
    }

    @Test
    public void two_space() {
        String origin = "　　12345\n　　abcde\n　　呵呵里个呵呵";
        String expect = "　　12345\n　　abcde\n　　呵呵里个呵呵";
        Assert.assertEquals(expect, formatExperience(origin));
    }

    @Test
    public void three_space() {
        String origin = "　　　12345\n　　　abcde\n　　　呵呵里个呵呵";
        String expect = "　　12345\n　　abcde\n　　呵呵里个呵呵";
        Assert.assertEquals(expect, formatExperience(origin));
    }

    @Test
    public void empty() {
        String origin = "";
        String expect = "";
        Assert.assertEquals(expect, formatExperience(origin));
    }

    @Test
    public void empty_line() {
        String origin = "　　　12345\n\n　　　呵呵里个呵呵";
        String expect = "　　12345\n　　\n　　呵呵里个呵呵";
        Assert.assertEquals(expect, formatExperience(origin));
    }
}
