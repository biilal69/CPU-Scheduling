using System;
using System.Collections.Generic;
using System.Diagnostics;

package scheduler.acs;

import java.util.ArrayList;
import java.util.List;
import model.Process;


public class PriorityTier
{


    private final List<Process> waiting = new ArrayList<>();

    public void add(Process p, int time)
    {
        p.setEnteredTier2At(time);   // aging needs this time later
        waiting.add(p);
    }

    public Process pollHighest()
    {
        if (waiting.isEmpty())
        {
            return null;
        }

        int bestIndex = 0;
        for (int i = 1; i < waiting.size(); i++)
        {
            if (comesBefore(waiting.get(i), waiting.get(bestIndex)))
            {
                bestIndex = i;
            }
        }
        return waiting.remove(bestIndex);
    }

    public boolean isEmpty()
    {
        return waiting.isEmpty();
    }

    public List<Process> getAll()
    {
        return waiting;
    }

    public String snapshot()
    {
        if (waiting.isEmpty())
        {
            return "Tier 2: [Empty]";
        }
        String text = "Tier 2: [";
        for (int i = 0; i < waiting.size(); i++)
        {
            if (i > 0)
            {
                text += ", ";
            }
            text += waiting.get(i).toString();
        }
        return text + "]";
    }
    private boolean comesBefore(Process a, Process b)
    {
        if (a.getPriority() != b.getPriority())
        {
            return a.getPriority() < b.getPriority();
        }
        if (a.getEnteredTier2At() != b.getEnteredTier2At())
        {
            return a.getEnteredTier2At() < b.getEnteredTier2At();
        }
        return a.getName().compareTo(b.getName()) < 0;
    }
}