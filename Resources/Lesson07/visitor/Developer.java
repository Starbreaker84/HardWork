package org.example.visitor;

import javax.xml.crypto.Data;

public interface Developer {
    public void create(ProjectClass projectClass);
    public void create(Database database);
    public void create(Test test);
}
