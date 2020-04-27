class AddOauthToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :oauth, :boolean, :default => false
  end
end
