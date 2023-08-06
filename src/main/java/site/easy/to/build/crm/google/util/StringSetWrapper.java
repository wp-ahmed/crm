package site.easy.to.build.crm.google.util;

import java.util.Set;

public class StringSetWrapper {
    private Set<String> stringSet;

    public StringSetWrapper(Set<String> stringSet) {
        this.stringSet = stringSet;
    }

    public Set<String> getStringSet() {
        return stringSet;
    }

    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }
}