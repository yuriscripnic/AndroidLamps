package physis.androidlamps;

import android.view.View;
import android.widget.RadioButton;

class Lamp{
    private RadioButton _rb =null;
    private char _value=0;

    Lamp (View rb){
        _rb = (RadioButton)rb;
    }

    void setValue(char value){
        _rb.setChecked(value == (char) 1);
        _value = value;
    }


    char getValue(){
        return _value;
    }
}