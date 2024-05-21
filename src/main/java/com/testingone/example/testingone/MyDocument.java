package com.testingone.example.testingone;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "collectionTestOne")
public class MyDocument {

    @Id
    private String id;
    private String created_at;
    private String text;
    private String source;
    private boolean truncated;
    // private User user;
    // private RetweetedStatus retweeted_status;
    private boolean is_quote_status;
    private int quote_count;
    private int reply_count;
    private int retweet_count;
    private int favorite_count;
    // private Entities entities;
    private boolean favorited;
    private boolean retweeted;
    // private EditHistory edit_history;
    // private EditControls edit_controls;
    private boolean editable;
    private String filter_level;
    private String lang;
    private String timestamp_ms;

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    // public User getUser() {
    //     return user;
    // }

    // public void setUser(User user) {
    //     this.user = user;
    // }

    // public RetweetedStatus getRetweeted_status() {
    //     return retweeted_status;
    // }

    // public void setRetweeted_status(RetweetedStatus retweeted_status) {
    //     this.retweeted_status = retweeted_status;
    // }

    public boolean isIs_quote_status() {
        return is_quote_status;
    }

    public void setIs_quote_status(boolean is_quote_status) {
        this.is_quote_status = is_quote_status;
    }

    public int getQuote_count() {
        return quote_count;
    }

    public void setQuote_count(int quote_count) {
        this.quote_count = quote_count;
    }

    public int getReply_count() {
        return reply_count;
    }

    public void setReply_count(int reply_count) {
        this.reply_count = reply_count;
    }

    public int getRetweet_count() {
        return retweet_count;
    }

    public void setRetweet_count(int retweet_count) {
        this.retweet_count = retweet_count;
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
    }

    // public Entities getEntities() {
    //     return entities;
    // }

    // public void setEntities(Entities entities) {
    //     this.entities = entities;
    // }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    // public EditHistory getEdit_history() {
    //     return edit_history;
    // }

    // public void setEdit_history(EditHistory edit_history) {
    //     this.edit_history = edit_history;
    // }

    // public EditControls getEdit_controls() {
    //     return edit_controls;
    // }

    // public void setEdit_controls(EditControls edit_controls) {
    //     this.edit_controls = edit_controls;
    // }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getFilter_level() {
        return filter_level;
    }

    public void setFilter_level(String filter_level) {
        this.filter_level = filter_level;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getTimestamp_ms() {
        return timestamp_ms;
    }

    public void setTimestamp_ms(String timestamp_ms) {
        this.timestamp_ms = timestamp_ms;
    }

    @Override
    public String toString() {
        return "MyDocument{" +
                "id='" + id + '\'' +
                ", created_at='" + created_at + '\'' +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", truncated=" + truncated +
                // ", user=" + user +
                // ", retweeted_status=" + retweeted_status +
                ", is_quote_status=" + is_quote_status +
                ", quote_count=" + quote_count +
                ", reply_count=" + reply_count +
                ", retweet_count=" + retweet_count +
                ", favorite_count=" + favorite_count +
                // ", entities=" + entities +
                ", favorited=" + favorited +
                ", retweeted=" + retweeted +
                // ", edit_history=" + edit_history +
                // ", edit_controls=" + edit_controls +
                ", editable=" + editable +
                ", filter_level='" + filter_level + '\'' +
                ", lang='" + lang + '\'' +
                ", timestamp_ms='" + timestamp_ms + '\'' +
                '}';
    }
}
