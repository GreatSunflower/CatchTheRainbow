package com.sunflower.catchtherainbow;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SuperComputer on 2/5/2017.
 */

//
public class Helper
{
    public static String secondToString(double seconds)
    {
        Date date = new Date((long)(seconds*1000));

        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }


}
