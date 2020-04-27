class AddOauthIdToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :id_oauth, :string, :default => nil
  end
end
