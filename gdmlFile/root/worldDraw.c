
void worldDraw() {
    gSystem->Load("libGeom");
    gSystem->Load("libGdml");
    TGeoManager *geom = TGeoManager::Import("test.gdml");

    TList *matList = geom->GetListOfMaterials();
    //cout << matList << endl;
    TIter next( matList );
    //TObject *mat;
    //TGeoMaterial *mat;
    /*mat = (TGeoMaterial) next();
    cout << mat << endl;
    mat = (TGeoMaterial) next();
    cout << mat << endl;*/
    while( mat = (TGeoMaterial*) next() ) { mat->SetTransparency( 50 ); }
    

    // manual, using name
    /*TGeoMaterial *mat = geom->GetMaterial("mat_vacuum");
      mat->SetTransparency( 50 );*/
    
    TGeoVolume *top = gGeoManager->GetTopVolume();
    top->SetLineColor( kRed );
    geom->SetTopVisible();
    TGeoVolume *box1 =  geom->GetVolume("vol_box1");
    box1->SetLineColor( kGreen );
    TGeoVolume *box2 =  geom->GetVolume("vol_box2");
    box2->SetLineColor( kYellow );
    top->Draw("ogl");
}
