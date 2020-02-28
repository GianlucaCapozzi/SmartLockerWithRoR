class AddTempPassToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :temp_pass, :string, :default => nil
    add_column :users, :reset_pass, :boolean, :default => false
  end
end
