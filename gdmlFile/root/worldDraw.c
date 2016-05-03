
void worldDraw() {
    gSystem->Load("libGeom");
    gSystem->Load("libGdml");
    TGeoManager *geom = TGeoManager::Import("survey_planes.gdml");

    TList *matList = geom->GetListOfMaterials();
    TIter matNext( matList );
    cout << "setting material transparencies\n";
    int transparencyDefault = 80;
    int transparencyFid = 0;
    while( mat = (TGeoMaterial*) matNext() )
    {
        TString *matName = new TString( mat->GetName() );
        if( matName->Contains("fid") )
        {
            cout << "fid " << transparencyFid;
            mat->SetTransparency( transparencyFid );
        }
        else if( matName->Contains("vec") )
        {
            cout << "vec " << transparencyFid;
            mat->SetTransparency( transparencyFid );
        }
        else
        {
            cout << "default " << transparencyDefault;
            mat->SetTransparency( transparencyDefault );
        }
        cout << " " << matName->Data() << "\n";
    }
    
    /*TGeoMaterial *mat = geom->GetMaterial("mat_vacuum");
      mat->SetTransparency( 50 );*/
    //TGeoMaterial *mat = new TGeoMaterial("mat_vacuum");
        
    TGeoVolume *top = geom->GetTopVolume();
    //top->SetLineColor( kWhite );
    //geom->SetTopVisible();
    
    TObjArray *volList = geom->GetListOfVolumes();
    TIter volNext( volList );
    cout << "setting volume colours\n";
    cout << "ref white\n";
    cout << "target black\n";
    cout << "modules red\n";
    cout << "fiducial_nominal blue\n";
    cout << "fiducial_ideal cyan\n";
    cout << "fiducial_measured green\n";
    
    while( vol = (TGeoVolume*) volNext() )
    {
        TString *volName = new TString( vol->GetName() );
        if( volName->Contains("ref") )
        {
            //cout << "ref";
            vol->SetLineColor( kWhite );
        }
        else if( volName->Contains("target") )
        {
            //cout << "target";
            vol->SetLineColor( kBlack );
        }
        else if( volName->Contains("mod") )
        {
            //cout << "mod";
            vol->SetLineColor( kRed );
        }
        else if( volName->Contains("fidn") || volName->Contains("vecn") )
        {
            //cout << "fidn / vecn";
            vol->SetLineColor( kBlue );
        }
        else if( volName->Contains("fidi") || volName->Contains("veci") )
        {
            //cout << "fidi";
            vol->SetLineColor( kCyan );
        }
        else if( volName->Contains("fidm") || volName->Contains("vecm") )
        {
            //cout << "fidm";
            vol->SetLineColor( kGreen );
        }
        else
        {
            //cout << "default";
            vol->SetLineColor( kGray );
        }
        //cout << " " << volName->Data() << "\n";
    }
    
    /*TGeoVolume *vol0 =  geom->GetVolume("vol_module0");
      vol0->SetLineColor( kGreen );
    TGeoVolume *vol1 =  geom->GetVolume("vol_module1");
    vol1->SetLineColor( kBlue );*/
    
    top->Draw("ogl");
}
