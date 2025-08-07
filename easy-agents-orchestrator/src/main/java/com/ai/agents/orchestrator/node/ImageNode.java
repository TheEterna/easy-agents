package com.ai.agents.orchestrator.node;

import org.springframework.ai.chat.client.*;

/**
 * 图像生成AI节点
 * 支持文本到图像的生成
 * 
 * @author 父亲的首席架构师
 * @time 2025/8/4
 */


public class ImageNode extends Node<String, String> {

    private String style;
    private int width = 1024;
    private int height = 1024;





    public ImageNode withStyle(String style) {
        this.style = style;
        return this;
    }

    public ImageNode withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public String executeNode() {
        return executeNode();
    }

//    @Override
//    public String executeNode(String prompt) {
//        String enhancedPrompt = style != null ?
//            String.format("%s, style: %s", prompt, style) :
//            prompt;
//
//        // 这里简化实现，实际应该调用ImageClient
//        return "Generated image URL for: " + enhancedPrompt;
//    }
}
