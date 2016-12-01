package com.cjq.tool.memorytour.bean;

/**
 * Created by KAT on 2016/10/27.
 */
public enum MemoryState {
    //关于以下五个状态，用一张表来形象的说明
    //     历史记录表  预期记忆表  待背诵章节表(虚拟)             说明
    //待诵     ×          ×          ×            原始状态，不参与任何记忆活动
    //已诵     √          ×          ×            完成记忆任务
    //未诵     ×          ×          √            会出现在新任务列表中
    //重诵     √          ×          √            对之前已完成的章节进行重新记忆，也会出现在新任务列表中
    //正诵     √          √          ×            正在记忆任务中
    TO_RECITE("待诵"),
    RECITED("已诵"),
    NOT_RECITE("未诵"),
    REPEAT_RECITE("重诵"),
    RECITING("正诵");

    private String label;

    public String getLabel() {
        return label;
    }

    MemoryState(String label) {
        this.label = label;
    }
}
