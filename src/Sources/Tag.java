package Sources;

public class Tag {
    String m_tag;

    public Tag(){
        m_tag="";
    }
    public Tag(String tag){
        m_tag= tag;
    }

    public String getTextTag(){
        return m_tag;
    }

    public void setTag(String tag){
        this.m_tag=tag;
    }
}
