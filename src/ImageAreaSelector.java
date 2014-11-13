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
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ImageAreaSelector {
    public ImageAreaSelector(int id) {
        ID = id;
    }

    public int ID;

    public Rectangle process() {
        IJ.selectWindow(ID);
        ImagePlus original = WindowManager.getCurrentImage();
        original.copy();

        IJ.newImage("ijmimt_temp", "8-bit", original.getWidth(), original.getHeight(), 1);
        ImagePlus newImage = WindowManager.getCurrentImage();
        newImage.paste();

        IJ.run("Deriche...", "alpha=1");

        newImage.changes = false;

        int[] ids = WindowManager.getIDList();
        for (int id : ids) {
            ImagePlus img = WindowManager.getImage(id);
            if (img == null || img == original)
                continue;
            String title = img.getTitle();
            if (!title.endsWith("norm 1.0") && !IJMIMT_.AllOpenImagesAtStart.contains(id))
                img.close();
        }

        IJ.run("Invert");
        IJ.run("8-bit");

        newImage = WindowManager.getCurrentImage();
        IJ.setThreshold(newImage, 0, 249);

        Roi[] rois = getParticles(newImage);
        ArrayList<Rectangle> areas = getMergedParticleAreas(rois);

        newImage = original.duplicate();
        new ImageWindow(newImage);
        newImage = WindowManager.getCurrentImage();

        AreaSelectionDialog dialog = new AreaSelectionDialog(areas);
        dialog.showDialog();
        newImage.changes = false;
        newImage.close();
        if (dialog.getCanceled())
            return null;
        return dialog.getResult();
    }

    private Roi[] getParticles(ImagePlus image) {
        ResultsTable table = new ResultsTable();
        RoiManager manager = new RoiManager();
        ParticleAnalyzer analyzer = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER, 0, table, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE);
        analyzer.analyze(image);
        Roi[] rois = manager.getRoisAsArray();
        Arrays.sort(rois, new Comparator<Roi>() {
            @Override
            public int compare(Roi roi, Roi roi2) {
                Rectangle bounds = roi.getBounds();
                Rectangle bounds2 = roi2.getBounds();
                return Double.compare(bounds.getWidth() * bounds.getHeight(), bounds2.getWidth() * bounds2.getHeight());
            }
        });
        image.changes = false;
        image.close();
        clearAndCloseRoiManager(manager);
        return rois;
    }

    private static final double MIN_PARTICLE_SIZE = 25.0;
    private static final double MAX_PARTICLE_SIZE = Double.POSITIVE_INFINITY;

    private void clearAndCloseRoiManager(RoiManager manager) {
        IJ.runMacro("roiManager", "delete");
        manager.close();
    }

    private ArrayList<Rectangle> getMergedParticleAreas(Roi[] rois) {
        ArrayList<Rectangle> areas = new ArrayList<Rectangle>();
        int count = Math.min(20, rois.length);
        for (int i = 0; i < count; i++) {
            Rectangle r = rois[rois.length - 2 - i].getBounds();
            boolean contained = false;
            for (int i2 = 0; i2 < areas.size() && !contained; i2++)
                contained = areas.get(i2).contains(r);
            if (!contained)
                areas.add(r);
        }
        return areas;
    }
}
