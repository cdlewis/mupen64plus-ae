/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae;

import java.io.File;
import java.util.List;

import paulscode.android.mupen64plusae.persistent.UserPrefs;
import paulscode.android.mupen64plusae.widget.RomItem;
import paulscode.android.mupen64plusae.widget.RomItemAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class GalleryActivity extends Activity implements OnItemClickListener
{
    // App data and user preferences
    private UserPrefs mUserPrefs = null;
    
    // Widgets
    private GridView mGridView;
    
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // Get user preferences
        mUserPrefs = new UserPrefs( this );
        mUserPrefs.enforceLocale( this );
        
        // Lay out the content and get the gridview widget
        setContentView( R.layout.gallery_activity );
        mGridView = (GridView) findViewById( R.id.gridview );
    }
    
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.gallery_activity, menu );
        return super.onCreateOptionsMenu( menu );
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.menuItem_refreshRoms:
                refreshRoms();
                return true;
                
            default:
                return super.onOptionsItemSelected( item );
        }
    }
    
    @Override
    public void onItemClick( AdapterView<?> parent, View v, int position, long id )
    {
        Toast.makeText( this, "" + position, Toast.LENGTH_SHORT ).show();
    }
    
    private void refreshRoms()
    {
        new Thread( new Runnable()
        {
            
            @Override
            public void run()
            {
                final List<RomItem> items = RomItem
                        .populate( GalleryActivity.this, new File( mUserPrefs.selectedGame ) );
                GalleryActivity.this.runOnUiThread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mGridView.setAdapter( new RomItemAdapter( GalleryActivity.this, R.id.text1, items ) );
                        mGridView.setOnItemClickListener( GalleryActivity.this );
                    }
                } );
            }
        } ).start();
    }
}
