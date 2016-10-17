package com.treem.treem.models.branch;

import android.graphics.Color;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Dan on 4/7/16.
 */
public class Branch implements Serializable {

    private static final String FIELD_POSITION = "position";
    private static final String FIELD_PARENT_ID = "parent_id";
    private static final String DEFAULT_COLOR = "8CBE31";
    public enum BranchPosition {
        Center(0),
        Left(1),
        TopLeft(2),
        TopRight(3),
        Right(4),
        BottomRight(5),
        BottomLeft(6),
        None(-1);

        private final int value;
        BranchPosition(int value) { this.value = value; }
        public int getIntValue() { return value; }
    }

    public enum ExploreType {
        News(0),
        Sports(1),
        Business(2),
        Entertainment(3),
        Media(4),
        Tech(5),
        Science(6);

        private final int value;
        ExploreType(int value) { this.value = value; }
        public int getIntValue() { return value; }
    }

    public Long id                      = 0L;       // 0 "Long"
    public Integer position             = 0;        // default is "center"
    public String color                 = DEFAULT_COLOR;
    public String name                  = null;
    public String icon                  = null;
    public String url                   = null;
    public Long parent_id               = null;
    public Integer ex_type              = null;
    public Long public_link_id          = null;

    @Expose(serialize = false, deserialize = false)
    public Branch parent                = null;
    public Branch [] children           = null;
    public Boolean customURL            = false;

    // public gets / sets for enums
    public void setPosition(Integer value){ this.position = value; }
    public void setPosition(BranchPosition position){ this.position = position.getIntValue(); }
    public BranchPosition getPosition(){
        if(this.position != null && this.position < ExploreType.values().length)
           return BranchPosition.values()[this.position];
        else return null;
    }

    public void setExploreType(Integer value){ this.ex_type = value; }
    public void setExploreType(ExploreType exploreType){ this.ex_type = exploreType.getIntValue(); }
    public ExploreType getExploreType(){
        if(this.ex_type != null && this.ex_type < ExploreType.values().length)
            return ExploreType.values()[this.ex_type];
        else return null;
    }

    public Integer getColor(){
        return getColor(color);
    }

    /**
     * Convert string color to int color
     * @param color the string color
     * @return the color
     */
    public static Integer getColor(String color){
        if (color==null)
            color = DEFAULT_COLOR;
        if (!color.startsWith("#")) { //check is color has a # sign at start
            if (color.length()<6) //add lead zeros if need
                color = "000000".substring(0,6-color.length())+color;
            color = "#" + color;
        }
        return Color.parseColor(color);
    }

    public String getPlacement(){
        JSONObject json = new JSONObject();

        try {
            json.put(FIELD_POSITION,position==null?0: position);
            json.put(FIELD_PARENT_ID,parent==null||parent.id==null?0:parent.id);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branch branch = (Branch) o;

        if (id != null ? !id.equals(branch.id) : branch.id != null) return false;
        if (position != null ? !position.equals(branch.position) : branch.position != null)
            return false;
        if (color != null ? !color.equals(branch.color) : branch.color != null) return false;
        if (name != null ? !name.equals(branch.name) : branch.name != null) return false;
        if (icon != null ? !icon.equals(branch.icon) : branch.icon != null) return false;
        if (url != null ? !url.equals(branch.url) : branch.url != null) return false;
        if (ex_type != null ? !ex_type.equals(branch.ex_type) : branch.ex_type != null)
            return false;
        if (public_link_id != null ? !public_link_id.equals(branch.public_link_id) : branch.public_link_id != null)
            return false;
        if (parent != null ? !parent.equals(branch.parent) : branch.parent != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(children, branch.children)) return false;
        return customURL != null ? customURL.equals(branch.customURL) : branch.customURL == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (ex_type != null ? ex_type.hashCode() : 0);
        result = 31 * result + (public_link_id != null ? public_link_id.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(children);
        result = 31 * result + (customURL != null ? customURL.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Branch{");
        sb.append("id=").append(id);
        sb.append(", position=").append(position);
        sb.append(", color='").append(color).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", ex_type=").append(ex_type);
        sb.append(", public_link_id=").append(public_link_id);
        sb.append(", parent=").append(parent);
        sb.append(", children=").append(Arrays.toString(children));
        sb.append(", customURL=").append(customURL);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Get color for the branch. Return default color for the NULL branch.
     * @param branch the branch to get color;
     * @return color of the branch
     */
    public static Integer getColor(Branch branch){
        if (branch!=null)
            return branch.getColor(); //get color of the branch if branch not null
        else
            return Branch.getColor(DEFAULT_COLOR); //get default color of the brnach
    }
}
