class AddImgToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :img, :string, :default => "R.drawable.com_facebook_profile_picture_blank_portrait"
  end
end
