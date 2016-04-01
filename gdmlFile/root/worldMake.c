
void worldMake() {
    gSystem->Load("libGeom");
    gSystem->Load("libGdml");
    
    new TGeoManager("world", "the simplest geometry");
    
    TGeoMaterial *mat = new TGeoMaterial("Vacuum", 0, 0, 0 );
    mat->SetTransparency( 50 );
    TGeoMedium *med = new TGeoMedium("Vacuum", 1, mat );
    
    TGeoVolume *top = gGeoManager->MakeBox("Top", med, 5.0, 5.0, 5.0 );
    gGeoManager->SetTopVolume( top );
    top->SetLineColor( kMagenta );

    TGeoVolume *box = gGeoManager->MakeBox("Box", med, 1.0, 2.0, 3.0 );
    box->SetLineColor( kRed );
    
    TGeoRotation *ro1 = new TGeoRotation( "ro1", 30.0, 0.0, 0.0 );
    TGeoCombiTrans *combi1 = new TGeoCombiTrans( 2.0, 0.0, 0.0, ro1 );
    top->AddNode( box, 1, combi1 );
    
    gGeoManager->CloseGeometry();
    
    gGeoManager->SetTopVisible();
    top->Draw("ogl");
    
    gGeoManager->Export("world.gdml");
}
