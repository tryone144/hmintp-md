package de.busse_apps.hmintpmd.common;

/*
 * Copyright 2015 Bernd Busse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;

import de.busse_apps.hmintpmd.R;

public class WSIKMCalculator {

    // --Commented out by Inspection (24.01.15 20:44):public static final double MAX_VALUE = 1776.0;
    // --Commented out by Inspection (24.01.15 20:44):public static final int MAX_DEGREE = 360;
    public static final int MAX_LEVEL = 6;

    public static double calculate(double lastTime, double massEaten, double bodymass) {
        double result = lastTime * 70 * (massEaten / 1000 / bodymass);
        if (Double.isInfinite(result)) {
            result = 0.0;
        } else if (Double.isNaN(result)) {
            result = 0.0;
        }
        return result;
    }

    public static double getDegreeForValue(double value) {
        double deg = 8.4 * Math.sqrt(value) + 6.0;
        if (value == 0) {
            deg = 0.0;
        }
        return deg;
    }

    public static int getLevelForValue(double value) {
        double lvl = (0.14 * Math.sqrt(value) + 0.15);
        if (lvl == 0.15) {
            return -1;
        } else {
            return (int) lvl;
        }
    }

    public static int getLevelForDegree(double degree) {
        int lvl = (int) ((degree * MAX_LEVEL) / 360);
        return lvl;
    }

    public static int getColorForLevel(Context context, int lvl) {
        int color;

        if (lvl < MAX_LEVEL / 3) {
            color = context.getResources().getColor(R.color.circle_meter_green);
        } else if (lvl < MAX_LEVEL * 2 / 3) {
            color = context.getResources().getColor(R.color.circle_meter_orange);
        } else {
            color = context.getResources().getColor(R.color.circle_meter_red);
        }

        return color;
    }

}
