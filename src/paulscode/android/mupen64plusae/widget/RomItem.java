package paulscode.android.mupen64plusae.widget;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import paulscode.android.mupen64plusae.persistent.AppData;
import paulscode.android.mupen64plusae.persistent.ConfigFile;
import paulscode.android.mupen64plusae.util.RomHeader;
import paulscode.android.mupen64plusae.util.RomInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class RomItem
{
    public static final String URL_TEMPLATE = "https://dl.dropboxusercontent.com/u/3899306/CoverArt/%s.png";
    
    public final File romPath;
    public final File coverPath;
    public final RomHeader header;
    public final RomInfo info;
    public final Bitmap coverArt;
    
    public RomItem( Context context, File romPath )
    {
        this.romPath = romPath;
        if( romPath != null )
        {
            this.coverPath = null;
            this.header = new RomHeader( romPath );
            this.info = new RomInfo( romPath, new ConfigFile( new AppData( context ).mupen64plus_ini ) );
            this.coverArt = downloadBitmap( info.goodName );
        }
        else
        {
            this.coverPath = null;
            this.header = null;
            this.info = null;
            this.coverArt = null;
        }
    }
    
    public static List<RomItem> populate( Context context, File startFile )
    {
        if( !startFile.exists() )
            return null;
        
        if( startFile.isFile() )
            startFile = startFile.getParentFile();
        
        List<RomItem> list = new ArrayList<RomItem>();
        String[] paths = startFile.list();
        if( paths != null )
        {
            for( String path : paths )
            {
                list.add( new RomItem( context, new File( startFile, path ) ) );
            }
        }
        
        return list;
    }
    
    private static Bitmap downloadBitmap( String goodName )
    {
        if( goodName == null )
            return null;
        
        String[] parts = goodName.split( "\\(" );
        String shortName = parts[0].trim().replace( ' ', '_' ).replace( "'", "" );
        
        URL url = null;
        URLConnection connection = null;
        InputStream stream = null;
        Bitmap bitmap = null;        
        try
        {
            url = new URL( String.format( URL_TEMPLATE, shortName ) );
            connection = url.openConnection();
            stream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream( stream );
        }
        catch( MalformedURLException e )
        {
            Log.w( "RomItem", "MalformedURLException: ", e);
        }
        catch( IOException e )
        {
            Log.w( "RomItem", "IOException: ", e);
        }
        finally
        {
            try
            {
                if( stream != null )
                    stream.close();
            }
            catch( IOException e )
            {
                Log.w( "RomItem", "IOException on close: ", e);
            }
        }
        
        return bitmap;
    }
}
