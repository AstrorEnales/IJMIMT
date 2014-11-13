/*
The MIT License (MIT)

Copyright (c) 2014 Marcel Friedrichs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;

import java.awt.*;
import java.util.ArrayList;

public class IJMIMT_ implements PlugIn {
    @Override
    public void run(String s) {
        AllOpenImagesAtStart = new ArrayList<Integer>();
        int[] ids = WindowManager.getIDList();
        for (int id : ids)
            AllOpenImagesAtStart.add(id);

        SettingsDialog settings = new SettingsDialog();
        settings.showDialog();
        if (settings.getCanceled())
            return;

        ids = settings.getSelectedIds();
        Rectangle[] areas = new Rectangle[ids.length];
        int maxWidth = 0;
        int maxHeight = 0;
        if (settings.getAllImagesSame()) {
            ImageAreaSelector selector = new ImageAreaSelector(ids[0]);
            Rectangle area = selector.process();
            if (area == null)
                return;
            maxWidth = area.width;
            maxHeight = area.height;
            for (int i = 0; i < areas.length; i++)
                areas[i] = area;
        } else {
            for (int i = 0; i < areas.length; i++) {
                ImageAreaSelector selector = new ImageAreaSelector(ids[i]);
                areas[i] = selector.process();
                if (areas[i] == null)
                    return;
                if (areas[i].width > maxWidth)
                    maxWidth = areas[i].width;
                if (areas[i].height > maxHeight)
                    maxHeight = areas[i].height;
            }
        }

        IJ.newImage("ijmimt_stack", "RGB", maxWidth, maxHeight, areas.length);
        ImagePlus stack = WindowManager.getCurrentImage();
        for (int i = 0; i < areas.length; i++) {
            Rectangle area = areas[i];
            IJ.selectWindow(ids[i]);
            IJ.makeRectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
            IJ.runMacro("Copy");
            stack.setSlice(i + 1);
            IJ.selectWindow("ijmimt_stack");
            IJ.runMacro("Paste");
        }

        MontageMaker montage = new MontageMaker();
        montage.makeMontage(stack, (areas.length / 2) + 1, 2, 1.0, 1, areas.length, 1, 1, true);

        stack.changes = false;
        stack.close();
    }

    public static ArrayList<Integer> AllOpenImagesAtStart;
}
