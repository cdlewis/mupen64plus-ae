package paulscode.android.mupen64plusae.widget;

import java.util.List;

import paulscode.android.mupen64plusae.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RomItemAdapter extends ArrayAdapter<RomItem>
{
    private final Context mContext;
    
    public RomItemAdapter( Context context, int textViewResourceId, List<RomItem> objects )
    {
        super( context, textViewResourceId, objects );
        
        mContext = context;
    }
    
    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = convertView;
        if( view == null )
        {
            view = inflater.inflate( R.layout.rom_item_adapter, parent, false );
        }
        
        RomItem item = getItem( position );
        if( item != null )
        {
            ImageView artView = (ImageView) view.findViewById( R.id.imageArt );
            if( item.coverPath != null && item.coverPath.exists() )
            {
                BitmapDrawable art = new BitmapDrawable( mContext.getResources(), item.coverPath.getAbsolutePath() );
                artView.setImageDrawable( art );
            }
            else if( item.coverArt != null )
            {
                artView.setImageBitmap( item.coverArt );
            }
            else
            {
                artView.setImageResource( R.drawable.default_coverart );
            }
            
            CharSequence text;
            
            TextView tv1 = (TextView) view.findViewById( R.id.text1 );
            // text = item.romPath.getName() + "\n" + item.header.name;
            text = item.info.goodName;
            tv1.setText( text );
            
            TextView tv2 = (TextView) view.findViewById( R.id.text2 );
            // text = item.header.crc1 + " " + item.header.crc2;
            text = item.info.crc;
            tv2.setText( text );
            
            TextView tv3 = (TextView) view.findViewById( R.id.text3 );
            text = item.info.md5;
            tv3.setText( text );
        }
        
        return view;
    }
}
