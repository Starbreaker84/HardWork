package org.example.visitor;

import java.util.ArrayList;
import java.util.List;

public class Project implements ProjectElement{
    private List<ProjectElement> elements;

    public Project() {
        elements = new ArrayList<>();
        elements.add(new ProjectClass());
        elements.add(new Database());
        elements.add(new Test());
    }

    @Override
    public void beWritten(Developer developer) {
        for (ProjectElement element : elements) {
            element.beWritten(developer);
        }
    }
}
