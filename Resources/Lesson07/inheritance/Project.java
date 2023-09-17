package org.example.inharitance;

import java.util.ArrayList;
import java.util.List;

public class Project extends ProjectElement{
    List<ProjectElement> projectElements;

    public Project () {
        projectElements = new ArrayList<>();
        projectElements.add(new ProjectClass());
        projectElements.add(new Database());
        projectElements.add(new Test());
    }

    @Override
    public void beWritten(JuniorDev developer) {
        for (ProjectElement element : projectElements) {
            element.beWritten(developer);
        }
    }

    @Override
    public void beWritten(SeniorDev developer) {
        for (ProjectElement element : projectElements) {
            element.beWritten(developer);
        }
    }
}
