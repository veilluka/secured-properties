package ch.cnc;

import java.util.List;

public class GuiProp implements Comparable<String> {



    public String get_name() {
        return _name;
    }
    public void set_name(String _name) {
        this._name = _name;
    }
    private String _name;
    public String get_label() {
        return _label;
    }
    public void set_label(String _label) {
        this._label = _label;
    }
    private String _label;
    public List<SecureProperty> get_secureProperties() {
        return _secureProperties;
    }
    public void set_secureProperties(List<SecureProperty> _secureProperties) {
        this._secureProperties = _secureProperties;
    }
    List<SecureProperty> _secureProperties = null;
    @Override
    public String toString() {
        return get_name();
    }

    @Override
    public int compareTo(String o) {
         return _name.compareTo(o);
    }

    public String get_style() {
        return _style;
    }

    public void set_style(String _style) {
        this._style = _style;
    }

    private String _style;

}
