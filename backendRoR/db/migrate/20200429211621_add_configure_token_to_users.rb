class AddConfigureTokenToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :configure_token, :string, :default => nil
  end
end
