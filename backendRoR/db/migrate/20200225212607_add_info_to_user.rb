class AddInfoToUser < ActiveRecord::Migration[6.0]
  def change
    add_column :users, :name, :string
    add_column :users, :surname, :string
    add_column :users, :age, :integer
    add_column :users, :weight, :decimal
  end
end
