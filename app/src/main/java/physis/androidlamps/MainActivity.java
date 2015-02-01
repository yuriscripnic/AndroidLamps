package physis.androidlamps;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {


    private Map<Integer,Lamp> lamps = new HashMap<>();
    private BluetoothController btController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lamps.put(R.id.lamp1, new Lamp(findViewById(R.id.lamp1)));
        lamps.put(R.id.lamp2, new Lamp(findViewById(R.id.lamp2)));
        lamps.put(R.id.lamp3, new Lamp(findViewById(R.id.lamp3)));
        lamps.put(R.id.lamp4, new Lamp(findViewById(R.id.lamp4)));
        lamps.put(R.id.lamp5, new Lamp(findViewById(R.id.lamp5)));
        lamps.put(R.id.lamp6, new Lamp(findViewById(R.id.lamp6)));
        lamps.put(R.id.lamp7, new Lamp(findViewById(R.id.lamp7)));
        lamps.put(R.id.lamp8, new Lamp(findViewById(R.id.lamp8)));
        lamps.put(R.id.lamp9, new Lamp(findViewById(R.id.lamp9)));

        btController = new BluetoothController(getApplicationContext());
        btController.connect();
        sendGetRequest();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        Lamp l = lamps.get(view.getId());
        l.setValue((checked ? (char) 1 : (char) 0));
    }

    void setLampsConfig(String data){
        int i=0;
        char[] char_data = data.toCharArray();
        for(Lamp l : lamps.values()) l.setValue(char_data[i++]);
    }

    public void sendDataButton(View view){
        sendPutRequest();
    }

    public void readDataButton(View view){
        sendGetRequest();
    }

    public void sendGetRequest(){
        btController.write("G");
        if ( btController.avaliable() >0) setLampsConfig(btController.read());
    }

    public void sendPutRequest(){
        String lamp_data="P";
        for(Lamp l : lamps.values()) lamp_data += l.getValue();
        btController.write(lamp_data);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        btController.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
