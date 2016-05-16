//
// Created by bijat on 10.05.2016.
//

#ifndef CHECKPRESENCE_IMAGE_UTILITY_H
#define CHECKPRESENCE_IMAGE_UTILITY_H

#include <cmath>
#include <vector>

#define MIN(a,b) ((a)<(b)?(a):(b))
#define MAX(a,b) ((a)>(b)?(a):(b))

std::vector<float> hsv2rgb(float hue, float sat, float val) {
    float red, grn, blu;
    float i, f, p, q, t;
    std::vector<float> result(3);

    if(val==0) {
        red = 0;
        grn = 0;
        blu = 0;
    } else {
        hue/=60;
        i = std::floor(hue);
        f = hue-i;
        p = val*(1-sat);
        q = val*(1-(sat*f));
        t = val*(1-(sat*(1-f)));
        if (i==0) {red=val; grn=t; blu=p;}
        else if (i==1) {red=q; grn=val; blu=p;}
        else if (i==2) {red=p; grn=val; blu=t;}
        else if (i==3) {red=p; grn=q; blu=val;}
        else if (i==4) {red=t; grn=p; blu=val;}
        else if (i==5) {red=val; grn=p; blu=q;}
    }
    result[0] = red;
    result[1] = grn;
    result[2] = blu;
    return result;
}

std::vector<float> rgb2hsv(float red, float grn, float blu){
    float hue, sat, val;
    float x, f, i;
    std::vector<float> result(3);

    x = MIN(MIN(red, grn), blu);
    val = MAX(MAX(red, grn), blu);
    if (x == val){
        hue = 0;
        sat = 0;
    }
    else {
        f = (red == x) ? grn-blu : ((grn == x) ? blu-red : red-grn);
        i = (red == x) ? 3 : ((grn == x) ? 5 : 1);
        hue = std::fmod((i-f/(val-x))*60, 360);
        sat = ((val-x)/val);
    }
    result[0] = hue;
    result[1] = sat;
    result[2] = val;
    return result;
}

#endif //CHECKPRESENCE_IMAGE_UTILITY_H
