package net.suteren;

import android.app.Activity;
import android.os.Bundle;

/* 
 * http://fgkavcihory.cateringmelodie.cz/cz/samoobsluzna-restaurace/denni-menu-tisk.php?kolikaty_tyden=44&zvoleny_den=1
 * http://fgkavcihory.cateringmelodie.cz/cz/samoobsluzna-restaurace/denni-menu-pristi-tyden-tisk.php?kolikaty_tyden=44&zvoleny_den=1
 */
public class FGDroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}