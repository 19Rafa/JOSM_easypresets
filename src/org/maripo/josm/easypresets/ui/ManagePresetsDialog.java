package org.maripo.josm.easypresets.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.maripo.josm.easypresets.data.EasyPresets;
import org.maripo.josm.easypresets.ui.editor.PresetEditorDialog;
import org.maripo.josm.easypresets.ui.editor.PresetEditorDialog.PresetEditorDialogListener;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

public class ManagePresetsDialog extends ExtendedDialog implements ListSelectionListener,
	PresetEditorDialogListener {
	private JButton editButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton reorderUpButton;
	private JButton reorderDownButton;

	public ManagePresetsDialog () {
		super(Main.parent, tr("Manage Custom Presets"));
		initUI();
	}
	TaggingPreset[] presets;
	private TaggingPreset selectedPreset;
	JList<TaggingPreset> list;
	private static class PresetRenderer extends JLabel implements ListCellRenderer<TaggingPreset> {
	    private final static Color selectionForeground;
	    private final static Color selectionBackground;
	    private final static Color textForeground;
	    private final static Color textBackground;
	    static {
	        selectionForeground = UIManager.getColor("Tree.selectionForeground");
	        selectionBackground = UIManager.getColor("Tree.selectionBackground");
	        textForeground = UIManager.getColor("Tree.textForeground");
	        textBackground = UIManager.getColor("Tree.textBackground");
	    }

		@Override
		public Component getListCellRendererComponent(JList<? extends TaggingPreset> list, TaggingPreset preset,
				int index, boolean isSelected, boolean cellHasFocus) {
			setIcon(preset.getIcon());
			setText(preset.getName());
			setOpaque(true);
			setBackground((isSelected)?selectionBackground:textBackground);
			setForeground((isSelected)?selectionForeground:textForeground);
			return this;
		}
	
	}
	private void initUI() {
		list = new JList();
		list.setCellRenderer(new PresetRenderer());
		final JPanel mainPane = new JPanel(new GridBagLayout());
		
		final JButton exportButton = new JButton(tr("Export"));
		exportButton.setToolTipText(tr("Export custom presets as a local XML file"));
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		mainPane.add(exportButton, GBC.eol().anchor(GridBagConstraints.EAST));

		final JPanel listPane = new JPanel(new GridBagLayout());
		final JPanel buttons = new JPanel(new GridBagLayout());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		
		list.addMouseListener(new MouseAdapter() {
			@Override
		    public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount()==2) {
					edit();
				}
			}
		});
		
		refreshList();
		JScrollPane listScroll = new JScrollPane(list);
		listScroll.setPreferredSize(new Dimension(320,420));
		listPane.add(listScroll, GBC.std());
		
		reorderUpButton = new JButton();
		reorderUpButton.setIcon(ImageProvider.get("dialogs", "up"));
		reorderUpButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reorderUp();
			}});
		
		reorderDownButton = new JButton();
		reorderDownButton.setIcon(ImageProvider.get("dialogs", "down"));
		reorderDownButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reorderDown();
			}});
		
		reorderUpButton.setEnabled(false);
		reorderDownButton.setEnabled(false);
		reorderUpButton.setToolTipText(tr("Move up"));
		reorderDownButton.setToolTipText(tr("Move down"));

		
		editButton = new JButton();
		editButton.setToolTipText(tr("Edit"));
		editButton.setIcon(ImageProvider.get("dialogs", "edit"));
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edit();
			}
		});
		editButton.setEnabled(false);
		
		copyButton = new JButton();
		copyButton.setToolTipText(tr("Copy"));
		copyButton.setIcon(ImageProvider.get("copy"));
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}
			
		});
		copyButton.setEnabled(false);
		

		deleteButton = new JButton();
		deleteButton.setToolTipText(tr("Delete"));
		deleteButton.setIcon(ImageProvider.get("dialogs", "delete"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (confirmDelete()) {
					delete();
				}
			}
		});
		deleteButton.setEnabled(false);

		buttons.add(reorderUpButton, GBC.eol());
		buttons.add(reorderDownButton, GBC.eol());
		buttons.add(editButton, GBC.eol());
		buttons.add(copyButton, GBC.eol());
		buttons.add(deleteButton, GBC.eol());
		listPane.add(buttons, GBC.eol().fill());
		mainPane.add(listPane, GBC.eol().fill());

		final JButton cancelButton = new JButton(tr("Close"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		mainPane.add(cancelButton, GBC.eol());
		setContent(mainPane);
	}

	private void refreshList() {
		presets = EasyPresets.getInstance().getPresets().toArray(new TaggingPreset[0]);
		list.clearSelection();
		list.setListData(presets);
	}

	private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(tr("Save Presets"));
        chooser.setFileFilter(new FileNameExtensionFilter("XML File", "xml"));
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
        	EasyPresets.getInstance().saveTo(chooser.getSelectedFile());
        }
	}
			
	protected void edit() {
		// Open 
		if (selectedPreset!=null) {
			new PresetEditorDialog(selectedPreset).showDialog(this);
		}
	}

    private boolean copy() {
    	if (selectedPreset!=null) {
    		TaggingPreset copiedPreset = EasyPresets.getInstance().duplicate(selectedPreset);
			refreshList();
        	return true;
    		
    	} else {
    		return false;
    	}
    }

    private boolean confirmDelete() {
        ExtendedDialog dialog = new ExtendedDialog(
                Main.parent,
                tr("Delete"),
                tr("Delete"), tr("Cancel")
        );
        dialog.setContent(tr("Are you sure you want to delete \"{0}\"?",selectedPreset.getName()));
        dialog.setButtonIcons("ok", "cancel");
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);
        dialog.setupDialog();
        dialog.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	dialog.toFront();
            }
        });
        return dialog.getValue() == 1;
    }
    
    
	private void delete() {
		if (selectedPreset!=null) {
			EasyPresets.getInstance().delete(selectedPreset);
			refreshList();
		}
	}
	
	@Override
	public void dispose() {
		EasyPresets.getInstance().saveIfNeeded();
		super.dispose();
	}
	
	protected void cancel() {
		dispose();
	}

	boolean isSelectionValid () {
		return !(list.getSelectedIndex() < 0 || list.getSelectedIndex() >= presets.length); 
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		reorderUpButton.setEnabled(e.getFirstIndex()>0);
		reorderDownButton.setEnabled(e.getFirstIndex()<presets.length-1);
		
		if (!isSelectionValid()) {
			editButton.setEnabled(false);
			deleteButton.setEnabled(false);
			return;
		}
		editButton.setEnabled(true);
		deleteButton.setEnabled(true);
		copyButton.setEnabled(true);
		select(presets[e.getFirstIndex()]);
	}

	private void select(TaggingPreset preset) {
		this.selectedPreset = preset;
	}
	
	private void reorderUp () {
		if (!isSelectionValid()) {
			return;
		}
		int index = list.getSelectedIndex();
		EasyPresets.getInstance().moveUp(index);
		refreshList();
		list.setSelectedIndex(index-1);
	}
	
	private void reorderDown () {
		if (!isSelectionValid()) {
			return;
		}
		int index = list.getSelectedIndex();
		EasyPresets.getInstance().moveDown(index);
		refreshList();
		list.setSelectedIndex(index+1);
	}

	/* Implementation of ManagePresetsDialogListener */
	@Override
	public void onCancel() {
		// Do nothing
	}

	@Override
	public void onSave() {
		refreshList();
	}
}
