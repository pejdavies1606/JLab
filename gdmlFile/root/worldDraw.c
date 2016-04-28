
void worldDraw() {
    gSystem->Load("libGeom");
    gSystem->Load("libGdml");
    TGeoManager *geom = TGeoManager::Import("survey_ideal_all.gdml");

    TList *matList = geom->GetListOfMaterials();
    TIter matNext( matList );
    while( mat = (TGeoMaterial*) matNext() )
    {
        mat->SetTransparency( 0 );
    }
    
    /*TGeoMaterial *mat = geom->GetMaterial("mat_vacuum");
      mat->SetTransparency( 50 );*/
    //TGeoMaterial *mat = new TGeoMaterial("mat_vacuum");
        
    TGeoVolume *top = geom->GetTopVolume();
    //top->SetLineColor( kWhite );
    //geom->SetTopVisible();
    
    TObjArray *volList = geom->GetListOfVolumes();
    TIter volNext( volList );
    while( vol = (TGeoVolume*) volNext() )
    {
        TString *volName = new TString( vol->GetName() );
        if( volName->Contains("ref") )
        {
            cout << "ref";
            vol->SetLineColor( kWhite );
        }
        else if( volName->Contains("target") )
        {
            cout << "target";
            vol->SetLineColor( kBlack );
        }
        else if( volName->Contains("mod") )
        {
            cout << "mod";
            vol->SetLineColor( kRed );
        }
        else if( volName->Contains("fidn") )
        {
            cout << "fidn";
            vol->SetLineColor( kOrange );
        }
        else if( volName->Contains("fidi") )
        {
            cout << "fidi";
            vol->SetLineColor( kSpring );
        }
        else
        {
            cout << "default";
            vol->SetLineColor( kGray );
        }
        cout << " " << volName->Data() << "\n";
    }
    
    /*TGeoVolume *vol0 =  geom->GetVolume("vol_module0");
      vol0->SetLineColor( kGreen );
    TGeoVolume *vol1 =  geom->GetVolume("vol_module1");
    vol1->SetLineColor( kBlue );*/
    
    top->Draw("ogl");
}
