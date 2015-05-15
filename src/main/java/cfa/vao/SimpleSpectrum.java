package cfa.vao;

import cfa.vo.speclib.SpectralDataset;
import com.owlike.genson.BeanView;

/**
 * Created by olaurino on 5/14/15.
 */
public class SimpleSpectrum implements BeanView<SpectralDataset> {
    public SimpleSpectrum() {}

    public String getTarget(SpectralDataset ds) {
        return ds.getTarget().getName().getValue();
    }

    public Double[] getX(SpectralDataset ds) {
        int len = ds.getData().size();
        Double[] x = new Double[len];
        for (int i=0; i<len; i++) {
            x[i] = ds.getData(i).getSpectralAxis().getValue().getValue();
        }

        return x;
    }

    public Double[] getY(SpectralDataset ds) {
        int len = ds.getData().size();
        Double[] y = new Double[len];
        for (int i=0; i<len; i++) {
            y[i] = ds.getData(i).getFluxAxis().getValue().getValue();
        }

        return y;
    }

    public Double[] getYErrLow(SpectralDataset ds) {
        int len = ds.getData().size();
        Double[] err = new Double[len];
        for (int i=0; i<len; i++) {
            try {
                Double flux = ds.getData(i).getFluxAxis().getValue().getValue();
                err[i] = flux - ds.getData(i).getFluxAxis().getAccuracy().getStatError().getValue();
            } catch(Exception ex) {
                err[i] = Double.NaN;
            }
        }
        return err;
    }

    public Double[] getYErrHigh(SpectralDataset ds) {
        int len = ds.getData().size();
        Double[] err = new Double[len];
        for (int i=0; i<len; i++) {
            try {
                Double flux = ds.getData(i).getFluxAxis().getValue().getValue();
                err[i] = flux + ds.getData(i).getFluxAxis().getAccuracy().getStatError().getValue();
            } catch(Exception ex) {
                err[i] = Double.NaN;
            }
        }
        return err;
    }

    public String getXUnits(SpectralDataset ds) {
        try {
            return ds.getData(0).getSpectralAxis().getValue().getUnit();
        } catch(Exception ex) {
            return "ERROR";
        }
    }

    public String getYUnits(SpectralDataset ds) {
        try {
            return ds.getData(0).getFluxAxis().getValue().getUnit();
        } catch(Exception ex) {
            return "ERROR";
        }
    }
}
