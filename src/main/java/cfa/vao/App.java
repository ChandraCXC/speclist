package cfa.vao;

import cfa.vo.speclib.*;
import cfa.vo.speclib.doc.ModelObjectFactory;
import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new Handler());
        server.setExecutor(null);
        server.start();
        System.out.println();
    }

    private static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            SpectralDataset ds = make_test_spectrum();
            Genson genson = make_genson();
            String json = genson.serialize(ds);

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
            File dir = new File(Handler.class.getResource("/").getPath());
            cfg.setDirectoryForTemplateLoading(dir);
            Template template = cfg.getTemplate("page.html");
            Map data = new HashMap<String, Object>();
            data.put("json", json);

            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            Writer w = new OutputStreamWriter(os);
            try {
                template.process(data, w);
                w.close();
            } catch (TemplateException e) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
//            os.write(response.getBytes());
            os.close();
        }
    }

    private static SpectralDataset make_test_spectrum(){
        ModelObjectFactory factory;
        SpectralDataset ds;
        Quantity q;

        factory = new ModelObjectFactory();
        ds = (SpectralDataset)factory.newInstance( SpectralDataset.class );

        try
        {
            // Top level metadata.. bypass description.
            ds.setSpectralSI("L");
            ds.setTimeSI("T");
            ds.setFluxSI("1.E-23 MT-2");
            ds.setDataProductType("Spectrum");
            ds.setDataProductSubtype( new Quantity(null,"L2 Spectrum",null,"meta.id"));

            // DataModel specification... manually set description
            ds.getDataModel().setName("Spectrum-2.0");
            ds.getDataModel().getName().setDescription("Data model name and version");
            ds.getDataModel().setPrefix("spec");
            ds.getDataModel().getPrefix().setDescription("Data model prefix tag");
            ds.getDataModel().setURL( new URL("http://www.ivoa.net/sample/spectral"));
            ds.getDataModel().getURL().setDescription("Reference URL for model");

            // DataID metadata
            ds.getDataID().setTitle("Sample Spectrum Instance");
            ds.getDataID().setCreator("UNKNOWN");
            ds.getDataID().setDatasetID(new URI("ivo://ADS/Sa.CXO#obs/12345"));
            //ds.getDataID().setCreatorDID();
            ds.getDataID().setObservationID("12345");
            ds.getDataID().setDate("2001-12-08T03:11:11");
            ds.getDataID().setVersion("001");
            ds.getDataID().setCreationType("Archival");
            //  NOTE: Alternatively, for List type.. you can get the List and add
            //        the Quantities, but you also have to set the model path
            //        to them... see Contributor below
            List<Quantity> items = new ArrayList<Quantity>();  // TODO - need VOList type to propogate modelpath.
            items.add(new Quantity("Collection1", "Chandra", null, null));
            items.add(new Quantity("Collection2", "X-Ray", null, null));
            items.add(new Quantity("Collection3", "Third Cambridge Catalogue of Radio Sources", null, null));
            ds.getDataID().setCollections(items);
            ds.getDataID().setLogo( new URL("http://www.cfa.harvard.edu/common/images/left/cfa-logo.gif"));
            items = ds.getDataID().getContributors();
            items.add(new Quantity("Contributor1", "This research has made use of software provided by the Chandra X-ray Center (CXC) in the application packages CIAO, ChIPS, and Sherpa.", null, null));
            items.get(0).setModelpath("SpectralDataset_DataID_Contributors");

            // Curation metadata
            ds.getCuration().setPublisher("Chandra X-ray Center");
            ds.getCuration().setPublisherID(new URI("ivo://cfa.harvard.edu"));
            ds.getCuration().setPublisherDID(new URI("ivo://cfa.harvard.edu/UNKNOWN"));
            ds.getCuration().setReleaseDate("2007-03-11T10:33:14");
            ds.getCuration().setVersion("002");
            ds.getCuration().setRights("public");
            items = new ArrayList<Quantity>();
            items.add(new Quantity(null, "UNKNOWN", null, null));
            ds.getCuration().setReferences(items);
            ds.getCuration().getContact().setName("CXC Help Desk");
            ds.getCuration().getContact().setEmail("cxchelp@head.cfa.harvard.edu");

            // Target metadata
            ds.getTarget().setName("3c273");
            ds.getTarget().setDescription("Optically Bright Quasar in Virgo");
            ds.getTarget().setRedshift(Double.valueOf("0.15833"));
            ds.getTarget().setTargetClass("Quasar");
            ds.getTarget().setSpectralClass("UNKNOWN");
            Double[] pos = new Double[]{ 187.2767, 2.0519};
            ds.getTarget().getPos().setValue(pos);
            ds.getTarget().setVarAmpl(Double.NaN);

            // ObsConfig metadata
            List<ObservingElement> elems = new ArrayList<ObservingElement>();
            elems.add( (ObservingElement)factory.newInstance( Facility.class ) );
            elems.add( (ObservingElement)factory.newInstance( Instrument.class ) );
            elems.add( (ObservingElement)factory.newInstance( Bandpass.class ) );
            elems.add( (ObservingElement)factory.newInstance( DataSource.class ) );
            elems.get(0).setName("CHANDRA");
            elems.get(1).setName("HRC");
            elems.get(2).setName("X-ray");
            elems.get(3).setName("pointed");
            ds.getObsConfig().setObservingElements(elems);

            // Proposal metadata
            ds.getProposal().setIdentifier("2001c2-p55422");

            // Derived metadata
            ds.getDerived().setSNR(Double.valueOf("1.3"));
            ds.getDerived().getRedshift().setValue(Double.valueOf("0.159"));
            ds.getDerived().getRedshift().setStatError(Double.valueOf("0.002"));
            ds.getDerived().getRedshift().setConfidence(Double.valueOf("0.999"));
            ds.getDerived().getVarAmpl().setValue(Double.valueOf("0.0001"));

        }
        catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (URISyntaxException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Data Points
        Double[] freq = new Double[]{ 8.32826233e+14,
                8.32190479e+14,
                8.31555695e+14,
                8.30921878e+14,
                8.30289027e+14
        };
        Double[] flux = new Double[]{ 3.72981229e-30,
                2.58023996e-30,
                3.49485448e-30,
                3.53532448e-30,
                3.53108340e-30
        };

        List<SPPoint> data = ds.getData();
        Quantity c10 = new Quantity("freq",freq[0],"Hz","em.freq");
        Quantity c11 = new Quantity("freq_err",3.0e+10,"Hz","stat.error;em.freq");
        Quantity c12 = new Quantity("freq_syserr",-1.0e+3,"Hz","stat.error.sys;em.freq");
        Quantity c13 = new Quantity("freq_res",10.0,"Hz","spect.resolution;em.freq");
        Quantity c14 = new Quantity("freq_binsiz",6.3575e+11,"Hz","em.freq;spect.binSize");

        Quantity c20 = new Quantity("flux",flux[0],"W.m**(-2).Hz**(-1)","phot.flux.density;em.freq");
        Quantity c21 = new Quantity("flux_err",1.0e-35,"W.m**(-2).Hz**(-1)","stat.error;phot.flux.density;em.freq");
        Quantity c22 = new Quantity("flux_syserr",-1.0e-40,"W.m**(-2).Hz**(-1)","stat.error.sys;phot.flux.density;em.freq");
        Quantity c23 = new Quantity("flux_qual",(Integer)0,null,"meta.code.qual;phot.flux.density;em.freq");

        Quantity c30 = new Quantity("bkg",Double.NaN,"W.m**(-2).Hz**(-1)","phot.flux.density;em.freq");
        Quantity c31 = new Quantity("bkg_qual",(Integer)1,null,"meta.code.qual;phot.flux.density;em.freq");

        for ( int ii=0; ii<3; ii++)
        {
            //TODO - Should not have to go back to the factory to generate
            //       new instances of List entries.. not sure how to tell
            //       it which flavor of content is wanted (ie subclass of List type)
            SPPoint point = (SPPoint)factory.newInstance( SPPoint.class );

            c10.setValue(freq[ii]);
            point.getSpectralAxis().setValue( c10 );
            point.getSpectralAxis().getAccuracy().setBinSize(c14);
            point.getSpectralAxis().getAccuracy().setStatError( c11 );
            point.getSpectralAxis().getAccuracy().setSysError( c12 );
            point.getSpectralAxis().getResolution().setRefVal( c13 );

            c20.setValue(flux[ii]);
            point.getFluxAxis().setValue( c20 );
            point.getFluxAxis().getAccuracy().setStatError( c21 );
            point.getFluxAxis().getAccuracy().setSysError( c22 );
            point.getFluxAxis().getAccuracy().setQualityStatus( c23 );
            List<Correction> corrs = new ArrayList<Correction>();
            ApFrac corr = (ApFrac)factory.newInstance( ApFrac.class );
            corr.setName("ApFrac");
            corr.setValue(0.75);
            corr.setApplied( Boolean.TRUE );
            corrs.add(corr);
            point.getFluxAxis().setCorrections(corrs);

            point.getBackgroundModel().setValue( c30 );
            point.getBackgroundModel().getAccuracy().setQualityStatus( c31 );

            data.add(point);
        }

        return ds;
    }

    private static class DoubleConverter implements Converter<Double> {

        public void serialize(Double number, ObjectWriter writer, Context ctx) throws Exception {

            if (Double.isNaN(number)) {
                writer.writeString("NaN");
            } else if (Double.POSITIVE_INFINITY == number) {
                writer.writeString("+Inf");
            } else if (Double.NEGATIVE_INFINITY ==number) {
                writer.writeString("-Inf");
            } else {
                writer.writeNumber(number);
            }

        }

        public Double deserialize(ObjectReader objectReader, Context context) throws Exception {
            return null;
        }
    }

        private static Genson make_genson() {
        return new GensonBuilder()
                .exclude("data", SpectralDataset.class)
                .exclude("description", Quantity.class)
                .exclude("name", Quantity.class)
                .exclude("utype", Quantity.class)
                .exclude("UCD", Quantity.class)
                .exclude("ID")
                .rename("sNR", "SNR")
                .rename("uRL", "URL")
                .rename("uCD", "UCD")
                .exclude("setID")
                .exclude("setModelPath")
                .exclude("setName")
                .exclude("setDescription")
                .exclude("setUCD")
                .exclude("setUnit")
                .exclude("setUtype")
                .exclude("setValue")
                .exclude("setDataProductType")
                .exclude("setDataProductSubtype")
                .exclude("setCalibLevel")
                .exclude("setDataModel")
                .exclude("setCuration")
                .exclude("setCharacterization")
                .exclude("setCoordSys")
                .exclude("setDataID")
                .exclude("setDerived")
                .exclude("setObsConfig")
                .exclude("setProposal")
                .exclude("setTarget")
                .exclude("setData")
                .exclude("setFluxSI")
                .exclude("setSpactralSI")
                .exclude("setTimeSI")
                .exclude("setRedshift")
                .exclude("setSNR")
                .exclude("setVarAmpl")
                .exclude("setConfidence")
                .exclude("setStatError")
                .exclude("setPrefix")
                .exclude("setURL")
                .exclude("setCollections")
                .exclude("setContributors")
                .exclude("setCreationType")
                .exclude("setCreator")
                .exclude("setCreatorDID")
                .exclude("setDatasetID")
                .exclude("setDate")
                .exclude("setLogo")
                .exclude("setObservationID")
                .exclude("setVersion")
                .exclude("setTitle")
                .exclude("setContact")
                .exclude("setEmail")
                .exclude("setPublisher")
                .exclude("setPublisherDID")
                .exclude("setPublisherID")
                .exclude("setReferences")
                .exclude("setReleaseDate")
                .exclude("setRights")
                .exclude("setCharacterizationAxes")
                .exclude("setEquinox")
                .exclude("setReferencePosition")
                .exclude("setZero")
                .exclude("setDopplerDefinition")
                .exclude("setSpectralSI")
                .exclude("setObservingElements")
                .exclude("setIdentified")
                .exclude("modelpath")
                .exclude("length")
                .useIndentation(true)
                .useRuntimeType(true)
                .setSkipNull(true)
                .withConverter(new DoubleConverter(), Double.class)
                .create();
    }
}
