/**
 * Copyright (C) 2012,2013 Wolfram Wagner
 *
 * This file is part of OpenRadar.
 *
 * OpenRadar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenRadar. If not, see <http://www.gnu.org/licenses/>.
 *
 * Diese Datei ist Teil von OpenRadar.
 *
 * OpenRadar ist Freie Software: Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder späteren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 *
 * OpenRadar wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE
 * GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.gui.status.radio;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import de.knewcleus.openradar.gui.GuiMasterController;
import de.knewcleus.openradar.gui.Palette;
import de.knewcleus.openradar.gui.setup.AirportData.FgComMode;

/**
 * This panel contains the radios in the status panel
 *
 * @author Wolfram Wagner
 */
public class RadioPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private GuiMasterController master;
    private RadioController radioManager;

    private Map<String,JButton> mapPTTButtons = new HashMap<String,JButton>() ;
    private Map<String,JLabel> mapRadioLabels = new HashMap<String,JLabel>() ;

    public RadioPanel(GuiMasterController master, RadioController radioManager) {
        this.master=master;
        this.radioManager=radioManager;
        radioManager.setRadioPanel(this);
        initRadios();

        this.setOpaque(false);
    }

    public void initRadios() {
        this.removeAll();
        this.setLayout(new GridBagLayout());

        int i=0;

        this.setLayout(new GridBagLayout());

        boolean isFgComInternal = master.getDataRegistry().getFgComMode() == FgComMode.Internal;

        for(RadioModel model : radioManager.getModels().values()) {

            JLabel lbRadioKey = new JLabel();
            lbRadioKey.setForeground(Color.lightGray);
            lbRadioKey.setName("lb"+model.getRadioKey());
            lbRadioKey.setText(model.getRadioKey());
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
            this.add(lbRadioKey,gridBagConstraints);
            mapRadioLabels.put(model.getRadioKey(), lbRadioKey);

            JComboBox<RadioFrequency> cbFrequencies = new JComboBox<RadioFrequency>();
            cbFrequencies.setName(model.getRadioKey());
            cbFrequencies.setToolTipText("right button lets you define a frequency");
            cbFrequencies.setModel(model);
            if(model.getSelectedItem()==null && model.getSize()>i) model.setSelectedItem(model.getElementAt(i));
            cbFrequencies.setEditable(false);
            cbFrequencies.setRenderer(new RadioFrequencyListCellRenderer());
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
            gridBagConstraints.ipadx = 20;
            gridBagConstraints.ipady = 4;
            this.add(cbFrequencies,gridBagConstraints);

            cbFrequencies.addActionListener(radioManager.getActionListener());
            cbFrequencies.addMouseListener(radioManager.getRadioModeMouseListener());

            JButton btPTT = new JButton();
            btPTT.setText("PTT");
            btPTT.setName("ptt-"+model.getRadioKey());
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = i;
            gridBagConstraints.weightx = isFgComInternal ? 0 : 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
            this.add(btPTT,gridBagConstraints);

            mapPTTButtons.put(model.getRadioKey(),btPTT);

            btPTT.addMouseListener(radioManager.getPttButtonListener());

            i++;
        }

        if(master.getDataRegistry().getFgComMode() != FgComMode.Off) {

            JPanel pnlRight = new JPanel();
            pnlRight.setOpaque(false);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1;
            gridBagConstraints.gridheight = i;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 10);
            this.add(pnlRight,gridBagConstraints);

            pnlRight.setLayout(new GridBagLayout());

            JLabel lbFreq = new JLabel();
            lbFreq.setForeground(Color.lightGray);
            lbFreq.setText("");//"910.00 Test FgCom"
            lbFreq.setFont(lbFreq.getFont().deriveFont(8));
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
            pnlRight.add(lbFreq,gridBagConstraints);

            if(isFgComInternal) {
                JLabel lbReset = new JLabel();
                lbReset.setName("lbRestart");
                lbReset.setText("Restart");
                lbReset.setFont(lbFreq.getFont().deriveFont(8));
                lbReset.setForeground(Palette.DESKTOP_FILTER_SELECTED);
                lbReset.setToolTipText("<html><body><b>Restart FgCom</b> if sound is distorted...<br/> Use it of users complain!</body></html>");
                lbReset.addMouseListener(radioManager.getPttButtonListener());
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
                gridBagConstraints.insets = new java.awt.Insets(4, 2, 2, 0);
                pnlRight.add(lbReset,gridBagConstraints);

                doLayout();
                if(getParent()!=null) {
                    getParent().invalidate();
                    ((JSplitPane)getParent().getParent().getParent()).invalidate();
                }
            }
        }
    }

    public void displayEnabledPTT(String radioKey, boolean enablePTT) {
        if (enablePTT) {
            mapPTTButtons.get(radioKey).setForeground(Color.red);
        } else {
            mapPTTButtons.get(radioKey).setForeground(Color.black);
        }
        mapPTTButtons.get(radioKey).repaint();
    }

    public void setRadioConnectedToServer(String radioKey, boolean success) {
        if(success) {
            mapRadioLabels.get(radioKey).setForeground(Color.lightGray);
            mapRadioLabels.get(radioKey).setToolTipText("FGCom connected");
        } else {
            mapRadioLabels.get(radioKey).setForeground(Color.red);
            mapRadioLabels.get(radioKey).setToolTipText("ERROR: FGCom does not accept frequency (Toggle Log Window ALT+L)");
        }
        mapRadioLabels.get(radioKey).repaint();
    }

}
