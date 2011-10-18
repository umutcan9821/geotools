package org.geotools.data.ogr;

import java.io.IOException;

import org.bridj.Pointer;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.GeometryFactory;

public class OGRFeatureStore extends ContentFeatureStore {

    OGRFeatureSource delegate;

    public OGRFeatureStore(ContentEntry entry, Query query) {
        super(entry, query);
        delegate = new OGRFeatureSource(entry, query);
    }

    @Override
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(Query query,
            int flags) throws IOException {
        Pointer dataSource = null;
        Pointer layer = null;
        boolean cleanup = true;
        try {
            // grab the layer
            String typeName = getEntry().getTypeName();
            dataSource = getDataStore().openOGRDataSource(false);
            layer = getDataStore().openOGRLayer(dataSource, typeName);

            FeatureReader<SimpleFeatureType, SimpleFeature> reader = delegate.getReaderInternal(query);
            GeometryFactory gf = delegate.getGeometryFactory(query);
            OGRDirectFeatureWriter result = new OGRDirectFeatureWriter(dataSource, layer, reader, getSchema(), gf);
            cleanup = false;
            return result;
        } finally {
            if (cleanup) {
                OGRUtils.releaseLayer(layer);
                OGRUtils.releaseDataSource(dataSource);
            }
        }
    }

    // ----------------------------------------------------------------------------------------
    // METHODS DELEGATED TO OGRFeatureSource
    // ----------------------------------------------------------------------------------------

    public OGRDataStore getDataStore() {
        return delegate.getDataStore();
    }

    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    public void setTransaction(Transaction transaction) {
        delegate.setTransaction(transaction);
    }

    public ResourceInfo getInfo() {
        return delegate.getInfo();
    }

    public QueryCapabilities getQueryCapabilities() {
        return delegate.getQueryCapabilities();
    }

    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return delegate.getBoundsInternal(query);
    }

    @Override
    protected int getCountInternal(Query query) throws IOException {
        return delegate.getCountInternal(query);
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)
            throws IOException {
        return delegate.getReaderInternal(query);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return delegate.buildFeatureType();
    }

}