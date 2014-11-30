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
import ij.gui.HistogramWindow;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;
import ij.process.ByteStatistics;
import ij.process.ImageStatistics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class IJMIMT_ implements PlugIn {
    @Override
    public void run(String s) {
        collectOpenImages();
        SettingsDialog settings = new SettingsDialog();
        settings.showDialog();
        if (settings.getCanceled())
            return;
        int[] ids = settings.getSelectedIds();
        if (ids.length <= 0) {
            IJ.error("At least one image has to be selected for IJMIMT to work!");
            return;
        }

        Rectangle[] areas = new Rectangle[ids.length];
        Point maxSize = selectAreasAndGetMaxSize(areas, settings, ids);
        if (maxSize == null)
            return;

        IJ.setBackgroundColor(255, 255, 255);
        int sliceCount = areas.length + (settings.getAddZProjection() ? 1 : 0);
        IJ.newImage(TEMP_TITLE, "RGB", maxSize.x, maxSize.y, sliceCount);
        ImagePlus stack = WindowManager.getCurrentImage();
        for (int i = 0; i < areas.length; i++) {
            Rectangle area = areas[i];
            IJ.selectWindow(ids[i]);
            IJ.makeRectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
            IJ.run("Copy");
            stack.setSlice(i + 1);
            IJ.selectWindow(TEMP_TITLE);
            IJ.run("Paste");
        }

        ArrayList<String> labels = settings.getSelectedLabels();
        if (settings.getAddZProjection()) {
            addZProjection(labels, stack, areas);
        }

        MontageMaker montage = new MontageMaker();
        int imagesPerRow = sliceCount <= 3 ? sliceCount : (sliceCount / 2) + (sliceCount % 2);
        int rowCount = sliceCount <= 3 ? 1 : 2;
        montage.makeMontage(stack, imagesPerRow, rowCount, 1.0, 1, sliceCount, 1, 1, false);
        stack.changes = false;
        stack.close();

        writeLabelsToMontage(labels, imagesPerRow, maxSize);
    }

    private void collectOpenImages() {
        AllOpenImagesAtStart = new ArrayList<Integer>();
        int[] ids = WindowManager.getIDList();
        if (ids != null)
            for (int id : ids)
                AllOpenImagesAtStart.add(id);
    }

    public static ArrayList<Integer> AllOpenImagesAtStart;

    private Point selectAreasAndGetMaxSize(Rectangle[] areas, SettingsDialog settings, int[] ids) {
        int maxWidth = 0;
        int maxHeight = 0;
        if (settings.getAllImagesSame()) {
            ImageAreaSelector selector = new ImageAreaSelector(getIdWithBestHistogram(ids));
            Rectangle area = selector.process();
            if (area == null)
                return null;
            maxWidth = area.width;
            maxHeight = area.height;
            for (int i = 0; i < areas.length; i++)
                areas[i] = area;
        } else {
            for (int i = 0; i < areas.length; i++) {
                ImageAreaSelector selector = new ImageAreaSelector(ids[i]);
                areas[i] = selector.process();
                if (areas[i] == null)
                    return null;
                if (areas[i].width > maxWidth)
                    maxWidth = areas[i].width;
                if (areas[i].height > maxHeight)
                    maxHeight = areas[i].height;
            }
        }
        return new Point(maxWidth, maxHeight);
    }

    private int getIdWithBestHistogram(int[] ids) {
        int bestIndex = 0;
        double bestRange = 0;
        double bestStdDeviation = 0;
        double bestMean = 0;
        for (int i = 0; i < ids.length; i++) {
            IJ.selectWindow(ids[i]);
            ImagePlus current = WindowManager.getCurrentImage();
            ImageStatistics stats = new ByteStatistics(new ByteProcessor(current.getProcessor(), false));
            HistogramWindow window = new HistogramWindow(TEMP_TITLE, current, stats);
            double range = stats.max - stats.min;
            if (i == 0 || (isRangeTolerable(range, bestRange) && isStdDevTolerable(stats.stdDev, bestStdDeviation) &&
                    isMeanDistTolerable(stats.mean, bestMean))) {
                bestIndex = i;
                bestRange = range;
                bestStdDeviation = stats.stdDev;
                bestMean = stats.mean;
            }
            window.close();
        }
        return ids[bestIndex];
    }

    private static final String TEMP_TITLE = "ijmimt_temp";

    private boolean isRangeTolerable(double newRange, double bestRange) {
        return newRange >= (bestRange * 0.85);
    }

    private boolean isStdDevTolerable(double newStdDev, double bestStdDeviation) {
        return newStdDev >= (bestStdDeviation * 0.85);
    }

    private boolean isMeanDistTolerable(double newMean, double bestMean) {
        return Math.abs(128 - newMean) <= (Math.abs(128 - bestMean) * 1.15);
    }

    private void addZProjection(ArrayList<String> labels, ImagePlus stack, Rectangle[] areas) {
        ZProjector zProjector = new ZProjector();
        zProjector.setImage(stack);
        zProjector.setStartSlice(1);
        zProjector.setStopSlice(areas.length);
        zProjector.setMethod(ZProjector.MAX_METHOD);
        zProjector.doRGBProjection();

        ImagePlus projectionResult = zProjector.getProjection();
        new ImageWindow(projectionResult);
        IJ.makeRectangle(0, 0, projectionResult.getWidth(), projectionResult.getHeight());
        IJ.run("Copy");
        stack.setSlice(areas.length + 1);
        IJ.selectWindow(TEMP_TITLE);
        IJ.run("Paste");
        projectionResult.changes = false;
        projectionResult.close();
        labels.add("z-projection");
    }

    private void writeLabelsToMontage(ArrayList<String> labels, int imagesPerRow, Point maxSize) {
        ImagePlus montageResult = WindowManager.getCurrentImage();
        Font font = new JLabel().getFont();
        IJ.setForegroundColor(255, 255, 255);
        int x = 5;
        int y = maxSize.y - font.getSize() + 5;
        for (int i = 0; i < labels.size(); i++) {
            TextRoi roi = new TextRoi(labels.get(i), x, y, font);
            montageResult.setRoi(roi);
            IJ.run("Fill");
            x += maxSize.x;
            if (i > 0 && ((i + 1) % imagesPerRow) == 0) {
                x = 5;
                y += maxSize.y;
            }
        }
        montageResult.setRoi((Roi) null);
    }
}
