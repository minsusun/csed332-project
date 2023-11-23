package com.mdeditor.sd;

import com.intellij.util.containers.LinkedListWithSum;
import org.apache.batik.bridge.Mark;

import java.util.LinkedList;
import java.util.List;

public class BlockManager {
    private final List<Block> blockList;
    private final MarkdownEditor mdEditor;

    public BlockManager(MarkdownEditor mdE) {
        this.blockList = new LinkedList<>();
        this.mdEditor = mdE;

        blockList.add(new Block(this));
        blockList.get(0).grabFocus();
    }

    /**
     * Handle Focus event
     * Block is created or deleted, request update to MarkdownEditor
     */
    public void update(Block block, BlockEvent e) {
        int idx = blockList.indexOf(block);

        switch (e) {
            case NEW_BLOCK -> {
                block.renderHTML();
                blockList.add(idx, new SingleLineBlock(this));
                blockList.get(idx+1).requestFocusInWindow();

                //mdEditor.updateUI();
            }
            case DELETE_BLOCK -> {
                if(idx > 0){
                    Block newFocusBlock = blockList.get(idx-1);
                    newFocusBlock.setMdText(newFocusBlock.getMdText() + block.getMdText());
                    blockList.remove(block);
                    block.destruct();
                    newFocusBlock.requestFocusInWindow();
                    //mdEditor.updateUI();
                }
            }
            case OUTFOCUS_BLOCK_UP -> {
                if(idx > 0){
                    block.renderHTML();
                    blockList.get(idx-1).requestFocusInWindow();
                }
            }
            case OUTFOCUS_BLOCK_DOWN -> {
                if(idx < blockList.size()-1){
                    block.renderHTML();
                    blockList.get(idx+1).requestFocusInWindow();
                }
            }
            default -> { throw new IllegalStateException("Unexpected value: " + e); }
        }
    }

    /**
     * Extract mdText sequentially from every block.
     * @return Full Markdown text which will be saved into the (virtual) file.
     */
    public String extractFullMd(){
        StringBuilder fullMd = new StringBuilder();
        for(Block block : blockList){
            fullMd.append(block.getMdText());
            fullMd.append("\n");
        }
        return fullMd.toString();
    }
}
