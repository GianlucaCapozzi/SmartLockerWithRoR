class AddInfoCompletedToUsers < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :info_completed, :boolean, :default => false
  end
end
