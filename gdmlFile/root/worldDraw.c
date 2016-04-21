
void worldDraw() {
    gSystem->Load("libGeom");
    gSystem->Load("libGdml");
    TGeoManager *geom = TGeoManager::Import("test.gdml");

    TList *matList = geom->GetListOfMaterials();
    TIter matNext( matList );
    while( mat = (TGeoMaterial*) matNext() )
    {
        mat->SetTransparency( 80 );
    }
    
    /*TGeoMaterial *mat = geom->GetMaterial("mat_vacuum");
      mat->SetTransparency( 50 );*/
    //TGeoMaterial *mat = new TGeoMaterial("mat_vacuum");
        
    TGeoVolume *top = geom->GetTopVolume();
    //top->SetLineColor( kWhite );
    //geom->SetTopVisible();
    
    TObjArray *volList = geom->GetListOfVolumes();
    TIter volNext( volList );
    while( vol = (TGeoVolume*) volNext() ) { vol->SetLineColor( kRed ); }
    
    /*TGeoVolume *vol0 =  geom->GetVolume("vol_module0");
      vol0->SetLineColor( kGreen );
    TGeoVolume *vol1 =  geom->GetVolume("vol_module1");
    vol1->SetLineColor( kBlue );*/
    
    top->Draw("ogl");
}
