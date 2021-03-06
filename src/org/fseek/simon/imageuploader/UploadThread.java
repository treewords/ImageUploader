/*
 * Copyright (C) 2012 Thedeath<www.fseek.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fseek.simon.imageuploader;

import org.fseek.simon.imageuploader.gui.MainFrame;
import org.fseek.simon.imageuploader.hoster.HosterImage;
import org.fseek.simon.imageuploader.hoster.ImageUploader;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JLabel;

public class UploadThread extends Thread
{

    private MainFrame main;
    private JLabel infoLabel;
    private BufferedImage buf;
    private File file;
    private ImageUploader upl;
    private HosterImage imgInfo;

    public UploadThread(MainFrame main, BufferedImage img, ImageUploader upl)
    {
        super("UploadThread");
        this.main = main;
        this.buf = img;
        this.infoLabel = main.getInfoLabel();
        this.upl = upl;
    }

    public UploadThread(MainFrame main, File file)
    {
        super("UploadThread");
        this.main = main;
        this.file = file;
        this.infoLabel = main.getInfoLabel();
    }

    @Override
    public void run()
    {
        Thread t = new Thread()
        {

            @Override
            public void run()
            {
                while (true)
                {
                    int progress = upl.getProgress();
                    switch (progress)
                    {
                        case -1:
                            infoLabel.setText("Something gone wrong, try again !");
                            return;
                        case 0:
                            infoLabel.setText("Converting...");
                            break;
                        case 25:
                            infoLabel.setText("Uploading...");
                            break;
                        case 50:
                            infoLabel.setText("Waiting for response...");
                            break;
                        case 75:
                            infoLabel.setText("Parse response...");
                            break;
                        case 100:
                            while(imgInfo == null)
                            {
                                try
                                {
                                    if(this.isInterrupted())return;
                                    Thread.sleep(100);
                                } catch (InterruptedException ex)
                                {
                                    this.interrupt();
                                    ex.printStackTrace();
                                }
                            }
                            infoLabel.setText("Finished ! ImageURL was copied in your Clipboard.");
                            main.setNormal(imgInfo.getImageURL());
                            setClipboard(imgInfo.getImageURL());
                            synchronized(this)
                            {
                                this.notify();
                            }
                            return;
                    }
                }
            }
        };
        t.setName("InfoUploadThread");
        t.start();
        if(file != null)
        {
            imgInfo = upl.uploadFile(file);
        }
        else
        {
            imgInfo = upl.uploadFile(buf);
        }
        if(imgInfo == null)
        {
            t.interrupt();
        }
    }

    public static void setClipboard(String str)
    {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }
}
