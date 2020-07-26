package org.maripo.josm.easypresets.data;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.EnumSet;

import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetItem;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetType;
import org.openstreetmap.josm.gui.tagging.presets.items.Check;
import org.openstreetmap.josm.gui.tagging.presets.items.Combo;
import org.openstreetmap.josm.gui.tagging.presets.items.Key;
import org.openstreetmap.josm.gui.tagging.presets.items.Label;
import org.openstreetmap.josm.gui.tagging.presets.items.Link;
import org.openstreetmap.josm.gui.tagging.presets.items.MultiSelect;
import org.openstreetmap.josm.gui.tagging.presets.items.Text;

public class EasyPreset extends TaggingPreset implements Cloneable {
	private static final long serialVersionUID = -7626914563011340418L;

	public EasyPreset() {
		super();
	}
	
	public static EasyPreset copy(TaggingPreset src) {
		EasyPreset preset = EasyPreset.clone(src);
		preset.name = tr("Copy of {0}", src.name);
		return preset;
	}

	public static EasyPreset getInstance(TaggingPreset src) {
		return EasyPreset.clone(src);
	}
	
	@Override
	public EasyPreset clone() {
		return EasyPreset.clone(this);
	}
	
	public static EasyPreset clone(TaggingPreset src) {
		EasyPreset preset = new EasyPreset();
		preset.name = src.name;
		preset.setIcon(src.iconName);
		for (TaggingPresetItem fromItem: src.data) {
			TaggingPresetItem item = clonePresetTag(fromItem);
			if (item != null) {
				preset.data.add(item);
			}
		}
		preset.types = EnumSet.noneOf(TaggingPresetType.class);
		if (src.types != null) {
			preset.types.addAll(src.types);
		}
		return preset;
	}
	
	private static TaggingPresetItem clonePresetTag(TaggingPresetItem itemFrom) {
		if (itemFrom instanceof Label) {
			Label itemTo = new Label();
			itemTo.text = ((Label) itemFrom).text;
			return itemTo;
		}
		else if (itemFrom instanceof Key) {
			Key key = (Key) itemFrom;
			Key itemTo = new Key();
			itemTo.key = key.key;
			itemTo.value = key.value;
			return itemTo;
		}
		else if (itemFrom instanceof Text) {
			Text text = (Text)itemFrom;
			Text itemTo = new Text();
			itemTo.text = text.text;
			itemTo.key = text.key;
			itemTo.default_ = text.default_;
			return itemTo;
		}
		else if (itemFrom instanceof Combo) {
			Combo combo = (Combo)itemFrom;
			Combo itemTo = new Combo();
			itemTo.text = combo.text;
			itemTo.key = combo.key;
			itemTo.values = combo.values;
			return itemTo;
		}
		else if (itemFrom instanceof MultiSelect) {
			MultiSelect multiselect = (MultiSelect)itemFrom;
			MultiSelect itemTo = new MultiSelect();
			itemTo.text = multiselect.text;
			itemTo.key = multiselect.key;
			itemTo.values = multiselect.values;
			return itemTo;
		}
		else if (itemFrom instanceof Check) {
			Check key = (Check) itemFrom;
			Check itemTo = new Check();
			itemTo.text = key.text;
			itemTo.key = key.key;
			return itemTo;
		}
		else if (itemFrom instanceof Link) {
			Link link = (Link)itemFrom;
			Link itemTo = new Link();
			itemTo.href = link.href;
			return itemTo;
		}
		return null;
	}
}