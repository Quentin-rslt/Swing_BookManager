package Sources;

import java.awt.*;
import java.util.ArrayList;

public class Tags extends Tag {
    private ArrayList<Tag> m_tags;

    public Tags() {
        this.m_tags = new ArrayList<>();
    }

    public ArrayList<Tag> getTags(){
        return m_tags;
    }
    public Tag getTag(int i){
        return m_tags.get(i);
    }
    public int getSizeTags(){
        return this.m_tags.size();
    }
    public boolean isEmpty(){
        return this.m_tags.isEmpty();
    }

    public void setTags(ArrayList<Tag> tag){
        this.m_tags = tag;
    }
    public void createTag(String strTag){
        Tag tag = new Tag(strTag);
        this.m_tags.add(tag);
    }
    public void addTag(Tag tag){
        this.m_tags.add(tag);
    }
}
